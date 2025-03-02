package org.example.service;

import org.example.AddRemove;
import org.example.AtomicStatus;
import org.example.Permission;
import org.example.dto.request.AddRemovePermissionToRoleRequest;
import org.example.dto.request.CreateRoleRequest;
import org.example.dto.response.RoleDto;
import org.example.dto.response.binary.node.RoleNode;
import org.example.dto.response.binary.tree.RolesTree;
import org.example.entity.Role;
import org.example.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class RoleManagementService {
    @Autowired
    public RoleRepository repository;

    @Autowired
    UserService userService;
    @Autowired
    RoleUserService roleUserService;

    @Autowired
    ServerService serverService;

    @Autowired
    ServerNotificationService serverNotificationService;

    @Autowired
    RoomScopeService roomScopeService;
    public RolesTree getServerRolesAsTree(String serverId, String token){
        /* We first check if the client has the permission to assign roles, if so for each role , we check if the client can assign this role and we add it as attribute
        * */
        String username = userService.getUser(token).getUsername();
        boolean userHasAssignRolePermission = roleUserService.doesUserHavePermissionInServer(Permission.ASSIGN_ROLE, serverId, username);
        RolesTree rolesTree = new RolesTree();
        repository.findByServerId(serverId)
                .forEach(
                        it -> {
                            RoleNode roleNode = new RoleNode(new RoleDto(it));
                            rolesTree.insert(roleNode);
                        }
                );
        return rolesTree;
    }



    public ResponseEntity<?> create(CreateRoleRequest createRoleRequest, String token){
        String principal = userService.getUser(token).getUsername();
        if (roleUserService.canUserCreateRoleWithRanking(createRoleRequest.getRanking(), createRoleRequest.getServerId(), principal))  // tested
            if (repository.findByServerIdAndName(createRoleRequest.getServerId(), createRoleRequest.getName()).isEmpty()){
                List<Role> serverRoles = repository.findByServerId(createRoleRequest.getServerId());
                        // ranking is logic                                                                            //
                if (createRoleRequest.getRanking() <= serverRoles.size() + 1 && createRoleRequest.getRanking() >= 1) {
                    this.incrementPostCreationRolesRankings(serverRoles, createRoleRequest.getRanking());  // tested
                    roleUserService.removePermissionsUserDoesntHave(createRoleRequest.getPermissions(), createRoleRequest.getServerId(), principal);
                    roomScopeService.removeScopesUserCannotAdd(createRoleRequest.getAddedRoomScopes(), principal, createRoleRequest.getServerId());
                    Role role = new Role(createRoleRequest);
                    repository.save(role);
                    serverNotificationService.notifyRoleCreation(role, role.getServerId(), principal);
                    return ResponseEntity.status(HttpStatus.CREATED)
                            .body(Map.of("roleId", role.getId()));
                }

                else
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .build();
            }

            else
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .build();
        else
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .build();
    }



    public HttpStatusCode delete(String roleId, String token){
        String principal = userService.getUser(token).getUsername();
        AtomicStatus atomicStatus = new AtomicStatus();
        repository.findById(roleId)
                .ifPresentOrElse(
                        it -> {
                            if (roleUserService.canUserDeleteRole(it, principal)){  // tested
                                repository.delete(it);
                                this.incrementPostDeletionRolesRankings(repository.findByServerId(it.getServerId()), it.getRanking());
                                serverNotificationService.notifyRoleDeletion(it, it.getServerId(), principal);
                                atomicStatus.set(HttpStatus.OK);
                            }
                            else atomicStatus.set(HttpStatus.FORBIDDEN);

                        }
                        ,
                        () -> atomicStatus.set(HttpStatus.NOT_FOUND)
                );
        return atomicStatus.get();
    }

    public void incrementPostCreationRolesRankings(List<Role> serverRoles, int order){  // set this function to be run after that the response is sent
        for (Role role : serverRoles)  // tested
            if (role.getRanking() >= order) {
                role.setRanking(role.getRanking() + 1);
                repository.save(role);
            }

    }

    private void incrementPostDeletionRolesRankings(List<Role> serverRoles, int order){
        System.out.println(serverRoles.size());
        for (Role role : serverRoles)  // tested
            if (role.getRanking() > order) {
                role.setRanking(role.getRanking() - 1);
                System.out.println(role.getRanking());
                repository.save(role);
            }
    }



    public HttpStatusCode addRemovePermissionToRole(AddRemovePermissionToRoleRequest request, String token, AddRemove action){
        String username = userService.getUser(token).getUsername();
        AtomicStatus atomicStatus = new AtomicStatus();
        repository.findById(request.getRoleId())
                .ifPresentOrElse(
                         it -> {
                             if (roleUserService.canUserAddPermissionToRole(it, request.getPermission(), username)){  // tested
                                 switch (action){
                                     case ADD -> {
                                         if (it.getPermissions().stream().anyMatch(p -> p == request.getPermission()))
                                             atomicStatus.set(HttpStatus.CONFLICT);
                                         else {
                                             it.addPermission(request.getPermission());
                                             repository.save(it);
                                             serverNotificationService.notifyRolePermissionAdding(request.getPermission(), it.getId(), it.getServerId(), username);
                                             atomicStatus.set(HttpStatus.OK);
                                         }
                                     }
                                     case REMOVE -> {
                                         atomicStatus.set(it.removePermission(request.getPermission()));
                                         repository.save(it);
                                         serverNotificationService.notifyRolePermissionRemoval(request.getPermission(), it.getId(), it.getServerId(), username);
                                     }
                                 }

                             }
                             else
                                 atomicStatus.set(HttpStatus.FORBIDDEN);

                         }
                        , () -> atomicStatus.set(HttpStatus.NOT_FOUND)
                );
        return atomicStatus.get();
    }

    public void deleteServerRoles(String serverId){
        repository.deleteAll(repository.findByServerId(serverId));
    }

    public List<Permission> getRolePermissions(String roleId){
        try {
            return repository.findById(roleId)
                    .get().getPermissions();
        } catch (NullPointerException e) {
            return new ArrayList<>();
        }

    }


}























