package org.example.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.Permission;
import org.example.config.RabbitConfig;
import org.example.dto.response.MemberDto;
import org.example.dto.response.RoleDto;
import org.example.dto.response.RoomScopeDto;
import org.example.entity.Role;
import org.example.entity.RoomScope;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.List;
import java.util.Map;

@Service
public class ServerNotificationService {
    @Autowired
    RabbitConfig rabbitConfig;

    @Autowired
    RabbitTemplate rabbitTemplate;
    @Autowired
    ObjectMapper objectMapper;


    private List<String> getAllServerConnectionsQueues(){
        return rabbitConfig.getAllQueues((String name, String key) -> name.split("/")[0].equals(key), "server-connections");
    }

    private void  notifyServer(Map<String, Object> body){
        for (String queueName : this.getAllServerConnectionsQueues())
            try {
                String id = queueName.split("/")[2];
                String exchange = "server-connections/exchange/" + id;
                String routingKey = "server-connections/routing-key/" + id;
                rabbitTemplate.convertAndSend(
                        exchange,
                        routingKey,
                        new org.springframework.amqp.core.Message(objectMapper.writeValueAsString(body).getBytes(StandardCharsets.UTF_8)),
                        m -> {
                            m.getMessageProperties().setContentType("application/json");
                            return m;
                        }
                );
            } catch (Exception e){
                e.printStackTrace();;
            }
    }

    public void notifyRoleCreation(Role role, String serverId, String actor) {
        try {
            this.notifyServer(
                    Map.of(
                            "action", "server-notification",
                            "type", "role_created",
                            "serverId", serverId,
                            "role", objectMapper.writeValueAsString(new RoleDto(role)),
                            "actor", actor,
                            "Authorization", rabbitConfig.messagingToken()
                    )
            );
        } catch (NoSuchAlgorithmException | InvalidKeySpecException | JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    public void notifyRoleDeletion(Role role, String serverId, String  actor) {
        try {
            this.notifyServer(
                    Map.of(
                            "action", "server-notification",
                            "type", "role_deleted",
                            "serverId", serverId,
                            "roleRanking", String.valueOf(role.getRanking()),
                            "actor", actor,
                            "Authorization", rabbitConfig.messagingToken()
                    )
            );
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
        }
    }

    public void notifyRoleAssignment(Role role, String username, String serverId, String actor) {
        try {
            this.notifyServer(
                    Map.of(
                            "action", "server-notification",
                            "type", "role_assigned",
                            "serverId", serverId,
                            "roleRanking", String.valueOf(role.getRanking()),
                            "username", username,
                            "actor", actor,
                            "Authorization", rabbitConfig.messagingToken()
                    )
            );
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
        }
    }

    public void notifyRoleRemoval(Role role, MemberDto member, String serverId, String actor) {
        try {
            this.notifyServer(
                    Map.of(
                            "action", "server-notification",
                            "type", "role_removed",
                            "serverId", serverId,
                            "roleRanking", String.valueOf(role.getRanking()),
                            "member", new ObjectMapper().writeValueAsString(member),
                            "actor", actor,
                            "Authorization", rabbitConfig.messagingToken()
                    )
            );
        } catch (NoSuchAlgorithmException | InvalidKeySpecException | JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    public void notifyRoomScopeCreation(RoomScope roomScope, String serverId,String actor) {
        try {
            this.notifyServer(
                    Map.of(
                            "action", "server-notification",
                            "type", "room_scope_created",
                            "serverId", serverId,
                            "roomScope", new ObjectMapper().writeValueAsString(new RoomScopeDto(roomScope)),
                            "actor", actor,
                            "Authorization", rabbitConfig.messagingToken()
                    )
            );
        } catch (JsonProcessingException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
        }
    }

    public void notifyRoomScopeDeletion(RoomScope roomScope, String actor) {
        try {
            this.notifyServer(
                    Map.of(
                            "action", "server-notification",
                            "type", "room_scope_deleted",
                            "serverId", roomScope.getRole().getServerId(),
                            "roomScope", new ObjectMapper().writeValueAsString(new RoomScopeDto(roomScope)),
                            "actor", actor,
                            "Authorization", rabbitConfig.messagingToken()
                    )
            );
        } catch (NoSuchAlgorithmException | InvalidKeySpecException | JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    public void notifyRolePermissionAdding(Permission permission, String roleId, String serverId, String actor) {
        try {
            this.notifyServer(
                    Map.of(
                            "action", "server-notification",
                            "type", "permission_added",
                            "serverId", serverId,
                            "permission", permission.name(),
                            "roleId", roleId,
                            "actor", actor,
                            "Authorization", rabbitConfig.messagingToken()
                    )
            );
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
        }
    }

    public void notifyRolePermissionRemoval(Permission permission, String roleId, String serverId, String actor) {
        try {
            this.notifyServer(
                    Map.of(
                            "action", "server-notification",
                            "type", "permission_removed",
                            "serverId", serverId,
                            "permission", permission.name(),
                            "roleId", roleId,
                            "actor", actor,
                            "Authorization", rabbitConfig.messagingToken()
                    )
            );
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
        }
    }





}
