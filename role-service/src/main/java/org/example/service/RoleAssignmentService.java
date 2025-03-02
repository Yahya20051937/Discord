package org.example.service;

import org.example.AtomicStatus;
import org.example.Permission;
import org.example.dto.response.MemberDto;
import org.example.entity.Role;
import org.example.entity.RoleUser;
import org.example.factory.RoleFactory;
import org.example.repository.RoleRepository;
import org.example.repository.RoleUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RoleAssignmentService {
    @Autowired
    RoleRepository repository;

    @Autowired
    UserService userService;
    @Autowired
    public
    RoleUserService roleUserService;

    @Autowired
    RoleUserRepository roleUserRepository;

    @Autowired
    ServerService serverService;

    @Autowired
    ServerNotificationService serverNotificationService;
    private boolean canPrincipalEditUserRole(Role role, String principal, String username){
        return
                serverService.isUserMemberOfThisServer(role.getServerId(), username) &&
                roleUserService.canUserAssignRole(principal, role);
    }


    public HttpStatusCode assignRoleToUser(String roleId, String token, String username){

        /* * here we check if the role is present, then there are three condition
         *  - we don't need to check if the principal is a member of the server because we are checking his permissions
         *  - the user that whose role is update must be a member of this role's server, this step is essential
         *  - the user can set role to user (check documentation) */
        if (roleUserRepository.findByUsernameAndRoleId(username, roleId).isEmpty()){
            AtomicStatus atomicStatus = new AtomicStatus();
            repository.findById(roleId)
                    .ifPresentOrElse(
                            it -> {
                                String principal = userService.getUser(token).getUsername();
                                if (this.canPrincipalEditUserRole(it, principal, username)) { // tested
                                    RoleUser roleUser = new RoleUser(username, it);
                                    roleUserRepository.save(roleUser);
                                    serverNotificationService.notifyRoleAssignment(it, username, it.getServerId(), principal);
                                    atomicStatus.setHttpStatusCode(HttpStatus.OK);
                                } else
                                    atomicStatus.setHttpStatusCode(HttpStatus.FORBIDDEN);
                            },
                            () -> atomicStatus.setHttpStatusCode(HttpStatus.NOT_FOUND)
                    );
            return atomicStatus.getHttpStatusCode();
        }
        return HttpStatus.CONFLICT;


    }

    public HttpStatusCode unAssignRoleToUser(String roleId, String token, String username){
        AtomicStatus atomicStatus = new AtomicStatus();
        repository.findById(roleId)
                .ifPresentOrElse(
                        it -> {
                            roleUserRepository.findByUsernameAndRoleId(username, roleId)
                                    .ifPresentOrElse(
                                            userRole -> {
                                                String principal = userService.getUser(token).getUsername();
                                                if (this.canPrincipalEditUserRole(it, principal, username)) {
                                                    serverNotificationService.notifyRoleRemoval(it, roleUserService.getMember(username, it.getServerId()), it.getServerId(), principal);
                                                    atomicStatus.setHttpStatusCode(HttpStatus.OK);
                                                    roleUserRepository.delete(userRole);
                                                }
                                                else
                                                    atomicStatus.setHttpStatusCode(HttpStatus.FORBIDDEN);


                                            },
                                            () -> atomicStatus.setHttpStatusCode(HttpStatus.CONFLICT)


                                    );
                        }
                        ,
                        () -> atomicStatus.setHttpStatusCode(HttpStatus.NOT_FOUND)
                );
        return atomicStatus.get();

    }

    public void createAndAssignOwnerRole(String serverId, String ownerName){
        Role ownerRole = RoleFactory.owner(serverId);
        repository.save(ownerRole);
        RoleUser roleUser = new RoleUser(ownerName, ownerRole);
        roleUserRepository.save(roleUser);
    }

    public void createMemberRole(String serverId){
        Role memberRole = RoleFactory.member(serverId);
        repository.save(memberRole);
    }

    public void assignMemberRole(String serverId, String username){
        /* The member role's id is the serverId
        * */
        repository.findById(serverId)
                .ifPresent( // this should be true.
                        it -> {
                            RoleUser roleUser = new RoleUser(username, it);
                            roleUserRepository.save(roleUser);
                        }
                );
    }

    public List<String> getClientAssignableRolesInServer(String serverId, String token){
        String principal = userService.getUser(token).getUsername();
        List<String> rolesIds = new ArrayList<>();
        if (roleUserService.doesUserHavePermissionInServer(Permission.ASSIGN_ROLE, serverId, principal))
            repository.findByServerId(serverId)
                    .forEach(
                            it -> {
                                if (roleUserService.canUserWithPermissionAssignRole(principal, it))  // tested   // edit this to avoid checking each time the permission...
                                    rolesIds.add(it.getId());
                            }
                    );
        return rolesIds;

    }

    public List<Integer> getMemberAssignedRolesInServer(String serverId, String member){
        List<Integer> rolesRankings = new ArrayList<>();
        roleUserRepository.findByRole_ServerId(serverId)
                .forEach(
                        it -> {
                            if (it.getUsername().equals(member))
                                rolesRankings.add(it.getRole().getRanking());
                        }
                );
        return rolesRankings;
    }


}
















