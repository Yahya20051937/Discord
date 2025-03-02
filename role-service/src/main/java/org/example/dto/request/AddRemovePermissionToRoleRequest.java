package org.example.dto.request;

import lombok.Data;
import org.example.Permission;

@Data
public class AddRemovePermissionToRoleRequest {
    private String roleId;
    private Permission permission;
}
