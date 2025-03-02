package org.example.dto.response;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.example.entity.RoleUser;

@Data
public class RoleUserDto implements Value {
    private String id;
    private String username;
    private String roleId;
    private String serverId;

    public RoleUserDto(RoleUser roleUser){
        this.id = roleUser.getId();
        this.username = roleUser.getUsername();
        this.roleId = roleUser.getRole().getId();
        this.serverId = roleUser.getRole().getServerId();
    }

    @Override
    public String getAttribute(String key) {
        return switch (key){
            case  "id" -> this.id;
            case "username" -> this.username;
            case "roleId" -> this.roleId;
            default -> null;
        } ;
    }

    @Override
    public void performAction(String action, Object... args) {

    }

    @Override
    public String getType() {
        return "roleUser";
    }
}
