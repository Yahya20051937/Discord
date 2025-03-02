package org.example.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.val;
import org.example.AddRemove;
import org.example.Permission;
import org.example.ScopeType;
import org.example.dto.request.AddRemovePermissionToRoleRequest;
import org.example.dto.request.AddRoomScopeRequest;
import org.example.dto.request.CreateRoleRequest;
import org.example.dto.response.RoomClientScopesDto;
import org.example.dto.response.RoomDto;
import org.example.dto.response.RoomScopeDto;
import org.example.dto.response.binary.tree.MembersTree;
import org.example.dto.response.binary.tree.RolesTree;
import org.example.dto.response.binary.tree.RoomPackagesTree;
import org.example.dto.response.binary.tree.Tree;
import org.example.service.RoleAssignmentService;
import org.example.service.RoleManagementService;
import org.example.service.RoleUserService;
import org.example.service.RoomScopeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
public class RoleController {
    @Autowired
    RoleManagementService roleManagementService;
    @Autowired
    RoleAssignmentService roleAssignmentService;

    @Autowired
    RoleUserService roleUserService;

    @Autowired
    RoomScopeService roomScopeService;


    @GetMapping("/api/role/auth/get/roles/{serverId}")
    public RolesTree getServerRoles(@PathVariable String serverId, HttpServletRequest request){
        return roleManagementService
                .getServerRolesAsTree(serverId, request.getHeader("Authorization"));
    }

   @GetMapping("/api/role/get-permissions/{roleId}")
   public List<Permission> getPermissions(@PathVariable String roleId){
        return roleManagementService.getRolePermissions(roleId);
   }

    @PostMapping("/api/role/auth/get/client-rooms-scopes")  //  also specify the main role ranking here.
    public List<RoomDto> getClientRoomsScopes(@RequestBody Map<String, List<Object>> requestBody, HttpServletRequest request){
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            List<RoomDto> rooms = requestBody.get("rooms")
                    .stream()
                    .map(it -> objectMapper.convertValue(it, new TypeReference<RoomDto>() {}))
                    .toList();
            return  roomScopeService.getClientRoomsScopes(rooms, request.getHeader("Authorization"));
        } catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    @PostMapping("/api/role/auth/update/client-rooms-scopes")  // test
    public Map<String, List<RoomClientScopesDto>> updateClientRoomsScopes(@RequestBody Map<String, List<RoomClientScopesDto>> requestBody, HttpServletRequest request){
        List<RoomClientScopesDto> rooms = requestBody.get("rooms");
        roomScopeService.updateRoomsClientScopes((requestBody.get("rooms")), request.getHeader("Authorization"));
        return Map.of("rooms", rooms);
    }

    @GetMapping("/api/role/auth/get/room-client-scopes")
    public List<ScopeType> getRoomClientScopes(@RequestParam String roomId, HttpServletRequest request){
        return roomScopeService.getRoomClientScopes(roomId, request.getHeader("Authorization"));
    }

    @GetMapping("/api/role/internal/get/room-client-scopes") // because in the server-connection-service, the token can expire
    public List<ScopeType> getRoomClientScopesInternal(@RequestParam String roomId, @RequestParam String username, HttpServletRequest request){
        return roomScopeService.getRoomClientScopesInternal(roomId, username, request.getHeader("token"));
    }

    @GetMapping("/api/role/auth/get/client-server-permissions") // not tested.
    public List<Permission> getClientServerPermissions(@RequestParam String serverId, HttpServletRequest request){
        return roleUserService.getClientServerPermissions(serverId, request.getHeader("Authorization"));
    }

    @PostMapping("/api/role/auth/create")
    public ResponseEntity<?> createRole(@RequestBody CreateRoleRequest createRoleRequest, HttpServletRequest request){
            // role id will be in the response
          return roleManagementService.create(createRoleRequest, request.getHeader("Authorization"));
    }

    @DeleteMapping("/api/role/auth/delete")
    public ResponseEntity<?> deleteRole(@RequestParam("id") String roleId, HttpServletRequest request){
        return ResponseEntity.status(
                roleManagementService.delete(roleId, request.getHeader("Authorization"))
        ).build();
    }

    /*@PutMapping("/api/role/update/ranking")
    public ResponseEntity<?> updateRanking(@RequestParam("id"))*/

    @PostMapping("/api/role/auth/add/permission")
    public ResponseEntity<?> addPermission(@RequestBody AddRemovePermissionToRoleRequest requestBody, HttpServletRequest request ){
        return ResponseEntity.status(
                roleManagementService.addRemovePermissionToRole(requestBody, request.getHeader("Authorization"), AddRemove.ADD)
        ).build();
    }

    @PostMapping("/api/role/auth/remove/permission")
    public ResponseEntity<?> removePermission(@RequestBody AddRemovePermissionToRoleRequest requestBody, HttpServletRequest request ){
        return ResponseEntity.status(
                roleManagementService.addRemovePermissionToRole(requestBody, request.getHeader("Authorization"), AddRemove.REMOVE)
        ).build();
    }

    @PutMapping("/api/role/auth/assign")
    public ResponseEntity<?> assignRole(@RequestParam("id") String roleId, @RequestParam("username") String username, HttpServletRequest request){
        return ResponseEntity.status(
                roleAssignmentService.assignRoleToUser(roleId, request.getHeader("Authorization"), username)
        ).build();
    }

    @PutMapping("/api/role/auth/unAssign")
    public ResponseEntity<?> unAssignRole(@RequestParam("id") String roleId, @RequestParam("username") String username, HttpServletRequest request){
        return ResponseEntity.status(
                roleAssignmentService.unAssignRoleToUser(roleId, request.getHeader("Authorization"), username)
        ).build();
    }

    @GetMapping("/api/role/auth/permission/delete-room")
    public Boolean canUserDeleteRoom(@RequestParam("roomId") String roomId, @RequestParam("serverId") String serverId, HttpServletRequest request){
        return roleUserService.canUserDeleteRoom(
                roomId,
                serverId,
                request.getHeader("Authorization")
        );
    }

    @PostMapping("/api/role/auth/permission/delete-rooms")
    public Boolean canUserDeleteRooms(@RequestParam("serverId") String serverId, @RequestBody Map<String, List<String>> body, HttpServletRequest request){
        return roleUserService.canUserDeleteRooms(
                serverId,
                request.getHeader("Authorization"),
                body.get("roomsIds")
            );
    }

    @GetMapping("/api/role/auth/permission/create-room")
    public Boolean canUserCreateRoom(@RequestParam("serverId") String serverId, HttpServletRequest request){
        return roleUserService.canUserCreateRoom(request.getHeader("Authorization"), serverId);
    }

    @GetMapping("/api/role/auth/permission/scope-in-room/{scope}")
    public Boolean doesUserHaveScopeInRoom(@PathVariable ScopeType scope, @RequestParam("roomId") String roomId, HttpServletRequest request){
        return roomScopeService.doesUserHaveScopeInRoom(request.getHeader("Authorization"), scope, roomId);
    }

    @PostMapping("/api/role/auth/scope/add")
    public ResponseEntity<?> addRoomScope(@RequestBody AddRoomScopeRequest requestBody, HttpServletRequest request){
        return ResponseEntity.status(
                roomScopeService.addRoomScope(requestBody, request.getHeader("Authorization"))
        ).build();
    }

    @DeleteMapping("/api/role/auth/scope/delete")
    public ResponseEntity<?> removeRoomScope(@RequestParam("id") String id, HttpServletRequest request){
        return ResponseEntity.status(
                roomScopeService.removeRoomScope(id, request.getHeader("Authorization"))
        ).build();
    }

    @GetMapping("/api/role/get/serverMembers/{serverId}")
    public List<MembersTree> getServerMembersTrees(@PathVariable String serverId){
        return roleUserService.getServerMembersTrees(serverId);
    }

    @GetMapping("/api/role/get/member/mainRole-ranking")
    public int getMemberMainRoleRanking(@RequestParam String username, @RequestParam String serverId){
        return roleUserService.getUserBiggestRoleInServerRanking(username, serverId);
    }

    @GetMapping("/api/role/get/assigned-roles")
    public Map<String, List<Integer>> getMemberAssignedRolesInServer(@RequestParam String member, @RequestParam String serverId){
        return Map.of("rolesRankings", roleAssignmentService.getMemberAssignedRolesInServer(serverId, member));
    }

    @GetMapping("/api/role/get-room-main-role-ranking/{roomId}")
    public Map<String, Integer> getRoomMainRoleRanking(@PathVariable String roomId){
        return Map.of("ranking", roleUserService.getRoomMainRoleRanking(roomId));
    }

    @GetMapping("/api/role/get-room-scopes/{roomId}")
    public Map<String, List<RoomScopeDto>> getRoomScopes(@PathVariable String roomId){
        return Map.of("scopes", roomScopeService.getRoomScopes(roomId));
    }

    @GetMapping("/api/role/auth/get/client-roles-ids/{serverId}")
    public Map<String, List<String>> getClientRolesIds(HttpServletRequest request, @PathVariable String serverId){
        return Map.of("rolesIds", roleUserService.getClientRolesIds(serverId, request.getHeader("Authorization")));
    }


}
