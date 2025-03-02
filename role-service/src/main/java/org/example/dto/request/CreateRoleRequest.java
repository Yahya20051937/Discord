package org.example.dto.request;

import lombok.Data;
import org.example.Permission;

import java.util.List;

@Data
public class CreateRoleRequest {
    private String name;
    private String serverId;
    private int ranking; // the order of power of this role in the server
    private List<Permission> permissions;
    private List<AddedRoomScopeDto> addedRoomScopes;
}
