package org.example.service;

import org.example.Permission;
import org.example.dto.response.MemberDto;
import org.example.dto.response.RoomInfo;
import org.example.dto.response.binary.tree.MembersTree;
import org.example.entity.Role;
import org.example.entity.RoleUser;
import org.example.entity.RoomScope;
import org.example.repository.RoleRepository;
import org.example.repository.RoleUserRepository;
import org.example.repository.RoomScopeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class RoleUserService {
    @Autowired
    public RoleUserRepository roleUserRepository;

    @Autowired
    UserService userService;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    RoomScopeRepository roomScopeRepository;

    public List<Permission> getClientServerPermissions(String serverId, String token){ // tested
        String username = userService.getUser(token).getUsername();
        return this.getClientServerPermissionsByUsername(serverId, username);
    }

    public List<Permission> getClientServerPermissionsByUsername(String serverId, String username){
        List<Permission> permissions = new ArrayList<>();
        roleUserRepository.findByUsername(username)
                .forEach(
                        it -> {
                            if (it.getRole().getServerId().equals(serverId))
                                for (Permission permission : it.getRole().getPermissions())
                                    if (!permissions.contains(permission))
                                        permissions.add(permission);
                        }
                );
        return permissions;
    }
    public boolean doesUserHavePermissionInServer(Permission permission, String serverId , String username){ // tested
        /*
        As we are dealing with a small dataset, we can work with linear search here, because the roles of a user will be a small list, and also the permission of the role
        * */
        List<Role> userRoles = roleUserRepository.findByUsernameAndRole_ServerId(username, serverId)
                .stream().map(RoleUser::getRole)
                .toList();
        for (Role role: userRoles)
            for (Permission rolePermission:role.getPermissions())
                if (permission.equals(rolePermission))
                    return true;
        return false;
    }

    public boolean canUserAssignRole(String username, Role role){ // tested
        /*
        * For a user to be able to set a role, he me a have the permission to add roles in the role's server and
        *  his biggest role must be bigger than the role, because even him doesn't have this role.  (by biggest I mean smaller , because 1 one is the biggest role)
        * */
        return this.doesUserHavePermissionInServer(Permission.ASSIGN_ROLE, role.getServerId(), username) &&
                this.getUserBiggestRoleInServerRanking(username, role.getServerId()) < role.getRanking();
    }

    public boolean canUserWithPermissionAssignRole(String username, Role role){
        return this.getUserBiggestRoleInServerRanking(username, role.getServerId()) < role.getRanking();
    }


    public int getUserBiggestRoleInServerRanking(String username, String serverId){
        List<RoleUser> userRoles = roleUserRepository.findByUsernameAndServerIdSorted(username, serverId);
        if (!userRoles.isEmpty())
            return userRoles.get(0)
                    .getRole()
                    .getRanking();
        else
            return Integer.MAX_VALUE;
    }

    public boolean canUserAddPermissionToRole(Role role, Permission permission, String username){ // tested
        // to add a permission to a role, the user must have the permission to add permission, and the permission (if not, he can assign to himself any permission he wants), and can assign the role.
        return  this.doesUserHavePermissionInServer(Permission.ADD_PERMISSION, role.getServerId(), username) && //tested
                this.doesUserHavePermissionInServer(permission, role.getServerId(), username) && // tested
                this.getUserBiggestRoleInServerRanking(username, role.getServerId()) < role.getRanking(); // tested

    }

    public boolean canUserAddRomeScope(Role role, String username, Boolean userHaveScopeInRoom){ // tested
        // to add or remove a room scope, in a role, the user must have the permission to add room scopes, and the user biggest role is bigger (lower) than the role. and user must have the scope he wants to give to this role.
        return
                userHaveScopeInRoom &&
                this.doesUserHavePermissionInServer(Permission.ADD_ROOM_SCOPE, role.getServerId(), username) &&  // tested,
                        this.getUserBiggestRoleInServerRanking(username, role.getServerId()) < role.getRanking();


    }

    public boolean canUserDeleteRoom(String roomId, String serverId, String token){
        /*
        * To delete a room, the user must have the permission to delete rooms, and  his biggest role, must be bigger than the room's biggest room-scope....
        * */
        String username = userService.getUser(token).getUsername();
        return this.getUserBiggestRoleInServerRanking(username, serverId) <= this.getRoomMainRoleRanking(roomId)  && this.doesUserHavePermissionInServer(Permission.DELETE_ROOM, serverId, username)
                ;
    }

    public boolean canUserDeleteRooms(String serverId, String token, List<String> roomsIds){
        String username = userService.getUser(token).getUsername();
        int userBiggestRoleRanking = this.getUserBiggestRoleInServerRanking(username, serverId);
        if (this.doesUserHavePermissionInServer(Permission.DELETE_ROOM, serverId, username)) {
            for (String id : roomsIds)
                if (userBiggestRoleRanking > this.getRoomMainRoleRanking(id))
                    return false;
            return true;
        }
        return false;

    }

    public boolean canUserCreateRoom(String token, String serverId){ // tested
        String username = userService.getUser(token).getUsername();
        return this.doesUserHavePermissionInServer(Permission.CREATE_ROOM, serverId, username);
    }

    public List<MembersTree> getServerMembersTrees(String serverId){
        List<MembersTree> membersTrees = new ArrayList<>();
        roleRepository.findByServerId(serverId)
                .forEach(
                        it -> {
                            membersTrees.add(new MembersTree(this.getRoleMainMembers(it)));
                        }
                );
        return membersTrees;
    }

    public List<MemberDto> getRoleMainMembers(Role role){
        // for each user that the role was assigned to, find other roles assigned to him in the server,
        // if only one's ranking is bigger (lower) than the role ranking, return in we reach the end,
        // it means that the role in the user main role , so add it
        List<MemberDto> mainMembers = new ArrayList<>();
        roleUserRepository.findByRoleId(role.getId())
                .forEach(
                        it -> {
                            for (RoleUser roleUser : roleUserRepository.findByUsernameAndRole_ServerId(it.getUsername(), role.getServerId()))
                                if (roleUser.getRole().getRanking() < role.getRanking())
                                    return;
                            mainMembers.add(new MemberDto(it.getUsername(), it.getRole().getRanking()));
                        }
                );
        return mainMembers;
    }

    public void removePermissionsUserDoesntHave(List<Permission> permissions, String serverId,String username){
        List<Permission> clientPermissions = this.getClientServerPermissionsByUsername(serverId, username);
        List<Integer> indexesToRemove = new ArrayList<>();
        for (int i =0 ; i < permissions.size(); i ++)
            if (!clientPermissions.contains(permissions.get(i)))
                indexesToRemove.add(i);

        for (Integer i : indexesToRemove)
            permissions.remove((int) i);

    }

    public Boolean canUserCreateRoleWithRanking(int ranking, String serverId, String username){
        return this.doesUserHavePermissionInServer(Permission.CREATE_ROLES, serverId, username) && this.getUserBiggestRoleInServerRanking(username, serverId) <= ranking;
    }

    public int getRoomMainRoleRanking(String roomId){
        List<RoomScope> roomScopesSortedByRoleRanking = roomScopeRepository.findByRoomIdSortedByOrder(roomId);
        if  (roomScopesSortedByRoleRanking.isEmpty())
            return Integer.MAX_VALUE;
        else
            return roomScopesSortedByRoleRanking.get(0).getRole().getRanking();
    }

    public Boolean canUserDeleteRole(Role role, String username){
        return this.doesUserHavePermissionInServer(Permission.DELETE_ROLE, role.getServerId(), username) && this.getUserBiggestRoleInServerRanking(username, role.getServerId()) <= role.getRanking();
    }

    private int getUserBiggestRoleRanking(String username, String serverId){
        AtomicInteger biggestRoleRanking = new AtomicInteger(Integer.MAX_VALUE);
        roleUserRepository.findByUsernameAndRole_ServerId(username, serverId)
                .forEach(it -> {
                    if  (it.getRole().getRanking() < biggestRoleRanking.get())
                        biggestRoleRanking.set(it.getRole().getRanking());
                });
        return biggestRoleRanking.get();
    }

    public MemberDto getMember(String username, String serverId){
        return new MemberDto(username, this.getUserBiggestRoleRanking(username, serverId));
    }

    public List<String> getClientRolesIds(String serverId, String token){
        String username = userService.getUser(token).getUsername();
        return roleUserRepository.findByUsernameAndRole_ServerId(username, serverId)
                .stream()
                .map(it -> it.getRole().getId())
                .toList();
    }



    }

