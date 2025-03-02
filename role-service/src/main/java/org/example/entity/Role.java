package org.example.entity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.Permission;
import org.example.dto.request.CreateRoleRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(name = "role", indexes = {
        @Index(name = "idx_server_id_name", columnList = "serverId, name"), // used to filter server_id, server_id & name
        @Index(name = "idx_server_id_ranking", columnList = "serverId, ranking") // used to filter server_id, server_id & order
})
public class Role {
    @Id
    private String id;
    private String name;
    private String serverId;
    private int ranking; // the order of power in this server, even though a user has a role that has a permission like ban users, he can only apply it to users that their biggest role is below his biggest role
    @Column(length = 1000)
    private List<Permission> permissions;   // ban users, ...

    @OneToMany(mappedBy = "role", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RoomScope> roomsScopes;

    @OneToMany(mappedBy = "role", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RoleUser> roleUsers;

    public Role(CreateRoleRequest createRoleRequest){
        this.id = UUID.randomUUID().toString();
        this.name = createRoleRequest.getName();
        this.serverId = createRoleRequest.getServerId();
        this.ranking = createRoleRequest.getRanking();
        this.permissions = createRoleRequest.getPermissions();
        this.roomsScopes = createRoleRequest
                .getAddedRoomScopes()
                .stream()
                .map(it -> new RoomScope(it, this))
                .toList();
        this.roleUsers = new ArrayList<>();
    }

    /*public String notificationJson() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> data = Map.of(
                "id", this.id,
                "name", this.name,
                "ranking", this.ranking,
                "permissions", this.permissions,
                "roomScopesIds" , this.roomsScopes
                        .stream()
                        .map(RoomScope::getId)
                        .toList(),
                "serverId", this.serverId
        );
        return objectMapper.writeValueAsString(data);

    }*/

    public void addPermission(Permission permission){
        this.permissions.add(permission);
    }

    public HttpStatusCode removePermission(Permission permission){
        for (Permission p: this.permissions)
            if (p == permission){
                this.permissions.remove(p);
                return HttpStatus.OK;
            }
        return HttpStatus.CONFLICT;
    }

}
