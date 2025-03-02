package org.example.service;

import org.example.*;
import org.example.dto.request.AddRoomScopeRequest;
import org.example.dto.request.AddedRoomScopeDto;
import org.example.dto.response.RoomDto;
import org.example.dto.response.RoomInfo;
import org.example.dto.response.RoomScopeDto;
import org.example.dto.response.RoomClientScopesDto;
import org.example.dto.response.binary.tree.RoomPackagesTree;
import org.example.entity.Role;
import org.example.entity.RoomScope;
import org.example.repository.RoleRepository;
import org.example.repository.RoleUserRepository;
import org.example.repository.RoomScopeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class RoomScopeService {
    @Autowired
    RoleUserRepository roleUserRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    RoleUserService roleUserService;

    @Autowired
    ServerNotificationService serverNotificationService;

    @Autowired
    UserService userService;

    @Autowired
    ServerService serverService;

    @Autowired
    public RoomScopeRepository repository;

    @Autowired
    WebClient webClient;

    public List<ScopeType> getRoomClientScopes(String roomId, String token){
        String username = userService.getUser(token).getUsername();
        return this.getRoomScopes(roomId, username);
    }

    public List<ScopeType> getRoomClientScopesInternal(String roomId,String username, String internalToken){
        if (userService.isMessageBrokerAuthorized(internalToken))
            return getRoomScopes(roomId, username);
        return new ArrayList<>();
    }

    public List<ScopeType> getRoomScopes(String roomId, String username){ // tested
        List<ScopeType> scopeTypes = new ArrayList<>();
        roleUserRepository.findByUsername(username)
                .forEach(
                        it -> {
                            repository.findByRoomIdAndRoleId(roomId, it.getRole().getId())
                                    .forEach(
                                            scope -> {
                                                if (!scopeTypes.contains(scope.getScopeType()))
                                                    scopeTypes.add(scope.getScopeType());
                                            }
                                    );
                        }
                );
        return scopeTypes;
    }


    public List<RoomDto> getClientRoomsScopes(List<RoomDto> rooms, String token){
        String username = userService.getUser(token).getUsername();
        for (RoomDto  room : rooms)
            if ( room.getRoomAccessType() == RoomAccessType.PRIVATE){      //we specify the room main role ranking, and   we add client room scopes.
                room.setMainRoleRanking(roleUserService.getRoomMainRoleRanking(room.getId()));
                roleUserRepository.findByUsername(username)
                        .forEach(
                                it -> {
                                    repository.findByRoomIdAndRoleId(room.getId(), it.getRole().getId())
                                            .forEach(
                                                roomScope -> {
                                                        if (!room.getClientScopes().contains(roomScope.getScopeType()))
                                                         room.getClientScopes().add(roomScope.getScopeType());
                                                    }
                                            );
                                }
                        );

            }
        return rooms;

    }





    public Boolean testDoesUserHaveScopeInRoom(String username, ScopeType scopeType, String roomId, String serverId){ // tested
        AtomicBoolean atomicBoolean = new AtomicBoolean(false);
        roleUserRepository.findByUsernameAndRole_ServerId(username, serverId)
                .forEach(
                        it -> it.getRole().getRoomsScopes()
                                .forEach(
                                        scope -> {
                                            if (scope.getRoomId().equals(roomId) && scope.getScopeType() == scopeType)
                                                atomicBoolean.set(true);
                                        }
                                )
                );
    return atomicBoolean.get();
    }

    public HttpStatusCode addRoomScope(AddRoomScopeRequest request, String token){ // to create a room scope, the room must be private, if the room is later switched  from private to public, we delete all rooms scopes.
        /*
        * Here, we first check if the role is present, if so we need to check that the role's server is the same as the room's server, because  in the roomScope entity
        * the role and room must have the same server_id, then to be able to add a room scope of this role, the principal must be able to assign this role (check documentation)
        and the principal must have the scope in the room.
        */

        String username = userService.getUser(token).getUsername();
        AtomicStatus atomicStatus = new AtomicStatus();
        roleRepository.findById(request.getRoleId())
                .ifPresentOrElse(
                        role -> {
                            RoomInfo roomInfo = this.getRoomInfo(request.getRoomId());
                            if (
                                    role.getServerId().equals(roomInfo.getServerId())
                                            && roomInfo.getRoomAccessType().equals(RoomAccessType.PRIVATE)
                                    && this.isRoomTypeCompatibleWithScopeType(roomInfo.getRoomType(), request.getScopeType())
                            )  // we check if room belongs to the role server, and that its access type is private
                                if (roleUserService.canUserAddRomeScope(
                                        role,
                                        username,
                                        this.checkIfUserHaveScopeInPrivateRoom(username, request.getScopeType() ,roomInfo.getServerId(), roomInfo.getId())
                                )){

                                    if (!this.isThereConflict(roomInfo.getId(), role.getId(), request.getScopeType())) {

                                        RoomScope roomScope = RoomScope.builder()
                                                .id(UUID.randomUUID().toString())
                                                .roomId(request.getRoomId())
                                                .role(role)
                                                .scopeType(request.getScopeType())
                                                .build();
                                        repository.save(roomScope);
                                        serverNotificationService.notifyRoomScopeCreation(roomScope, role.getServerId(), username);
                                        atomicStatus.set(HttpStatus.OK);
                                    }
                                    else
                                        atomicStatus.set(HttpStatus.CONFLICT);
                                }
                                else
                                    atomicStatus.set(HttpStatus.FORBIDDEN);
                            else
                                atomicStatus.set(HttpStatus.BAD_REQUEST);

                        }

                        ,
                        () -> atomicStatus.set(HttpStatus.NOT_FOUND)
                );
        return atomicStatus.get();
    }

    public HttpStatusCode removeRoomScope(String roomScopeId, String token){
        String username = userService.getUser(token).getUsername();
        AtomicStatus atomicStatus = new AtomicStatus();
        repository.findById(roomScopeId)
                .ifPresentOrElse(
                        it -> {
                            if (roleUserService.canUserAddRomeScope( // tested
                                    it.getRole(),
                                    username,
                                    this.checkIfUserHaveScopeInPrivateRoom(username, it.getScopeType(), it.getRole().getServerId(), it.getRoomId())
                            )){
                                repository.delete(it);
                                serverNotificationService.notifyRoomScopeDeletion(it, username);
                                atomicStatus.set(HttpStatus.OK);
                            }
                            else
                                atomicStatus.set(HttpStatus.FORBIDDEN);
                        }
                        ,
                        () -> atomicStatus.set(HttpStatus.NOT_FOUND)
                );
        return atomicStatus.get();
    }

    public void deleteRoomScopes(String roomId){
        repository.findByRoomId(roomId)
                .forEach(
                        it -> repository.delete(it)
                );
    }

    private RoomInfo getRoomInfo(String roomId){
        return webClient.get()
                .uri("http://room-service/api/room/getInfo?roomId=" + roomId)
                .retrieve()
                .bodyToMono(RoomInfo.class)
                .block();
    }


    public Boolean doesUserHaveAllScopesInRoom(String roomId, String token){ // because there is no join scope in textual rooms, and no read and write in vocal rooms, we check if it has either all vocal scopes or all text scopes.
        String username = userService.getUser(token).getUsername();
        return this.checkIfUserHaveAllScopesInRoom(roomId, username);
    }

    public Boolean checkIfUserHaveAllScopesInRoom(String roomId, String username){
        RoomInfo roomInfo = this.getRoomInfo(roomId);
        return (this.checkIfUserHaveScopeInRoom(username, ScopeType.READ, roomInfo) && this.checkIfUserHaveScopeInRoom(username, ScopeType.WRITE, roomInfo))
                || (this.checkIfUserHaveScopeInRoom(username, ScopeType.JOIN, roomInfo));
    }


    public Boolean doesUserHaveAllScopesInAllRooms(List<String> roomsIds, String token){
        String username = userService.getUser(token).getUsername();
        for (String roomId : roomsIds)
            if (!this.checkIfUserHaveAllScopesInRoom(roomId, username))
                return false;
        return true;
    }

    public Boolean doesUserHaveScopeInRoom(String token, ScopeType scopeType, String roomId){
        String username = userService.getUser(token).getUsername();
        RoomInfo roomInfo = this.getRoomInfo(roomId);
        return this.checkIfUserHaveScopeInRoom(username, scopeType, roomInfo);
    }



    private Boolean checkIfUserHaveScopeInRoom(String username, ScopeType scopeType, RoomInfo roomInfo){
        if (roomInfo != null) {
            if (roomInfo.getRoomAccessType().equals(RoomAccessType.PUBLIC))  // if the room is public, we don't have to check anything, and even if we check, we'll find nothing (public room no room scopes)
                return true;
            return checkIfUserHaveScopeInPrivateRoom(username, scopeType, roomInfo.getServerId(), roomInfo.getId());
        }
        return false;
    }

    private Boolean checkIfUserHaveScopeInPrivateRoom(String username, ScopeType scopeType, String serverId, String roomId){
        AtomicBoolean atomicBoolean = new AtomicBoolean(false);
        roleUserRepository.findByUsernameAndRole_ServerId(username, serverId)
                .forEach(
                        it -> it.getRole().getRoomsScopes()
                                .forEach(
                                        scope -> {
                                            if (scope.getRoomId().equals(roomId) && scope.getScopeType() == scopeType) {
                                                atomicBoolean.set(true);
                                            }
                                        }
                                )
                );

        return atomicBoolean.get();
    }

    private boolean isRoomTypeCompatibleWithScopeType(RoomType roomType, ScopeType scopeType){
        return (roomType == RoomType.TEXTUAL && (scopeType == ScopeType.WRITE || scopeType == ScopeType.READ)) ||
                (roomType == RoomType.VOCAL && scopeType == ScopeType.JOIN);
    }

    private boolean isThereConflict(String roomId, String roleId, ScopeType scopeType){
        for (RoomScope roomScope :  repository.findByRoomIdAndRoleId(roomId, roleId))
            if (roomScope.getScopeType() == scopeType)
                return true;
        return false;

    }

    public void updateRoomsClientScopes(RoomPackagesTree roomPackagesTree, String token){ // test
        roomPackagesTree.performToAll("update-client-scopes", this, token);
    }

    public void removeScopesUserCannotAdd(List<AddedRoomScopeDto> addedRoomScopes, String username, String serverId){
        List<Integer> indexToRemove = new ArrayList<>();  // remove the scope that user doesn't have.
        for (int i = 0 ; i < addedRoomScopes.size() ; i ++)
            if (!this.checkIfUserHaveScopeInPrivateRoom(username, addedRoomScopes.get(i).getScopeType(), serverId , addedRoomScopes.get(i).getRoomId()))
                indexToRemove.add(i);
        for (Integer i : indexToRemove)
            addedRoomScopes.remove((int) i);
    }

    public void addScopesToRoomForUserBiggestRole(String roomId, String username){  // after room creation, this will be called to give privileges to user who created it.
        RoomInfo roomInfo = this.getRoomInfo(roomId);  // we don't have to check if the room is private, we are sure.
        Role highestRole = roleUserRepository.findByUsernameAndServerIdSorted(username, roomInfo.getServerId())
                .get(0).getRole();
        List<ScopeType> scopes = (roomInfo.getRoomType()  == RoomType.TEXTUAL) ? List.of(ScopeType.READ, ScopeType.WRITE) : List.of(ScopeType.JOIN);
        for (ScopeType scope : scopes) {
            repository.save(
                    RoomScope.builder()
                            .id(UUID.randomUUID().toString())
                            .roomId(roomId)
                            .role(highestRole)
                            .scopeType(scope)
                            .build()
            );
        }


    }

    public List<RoomScopeDto> getRoomScopes(String roomId){
        return repository
                .findByRoomId(roomId)
                .stream()
                .map(RoomScopeDto::new)
                .toList();
    }

    public void updateRoomsClientScopes(List<RoomClientScopesDto> rooms, String token){
        String username = userService.getUser(token).getUsername();
        rooms.forEach(
                it -> {
                    roleUserRepository.findByUsername(username)
                            .forEach(
                                    r -> {
                                        repository.findByRoomIdAndRoleId(it.getRoomId(), r.getRole().getId())
                                                .forEach(
                                                        roomScope -> {
                                                            if (!it.getClientScopes().contains(roomScope.getScopeType()))
                                                                it.getClientScopes().add(roomScope.getScopeType());
                                                        }
                                                );
                                    }
                            );
                }
        );
    }








}





























