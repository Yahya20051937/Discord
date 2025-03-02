package org.example.dto.response;

import lombok.Data;
import org.example.Permission;
import org.example.entity.Role;
import org.example.entity.RoomScope;

import java.util.List;

@Data
public class RoleDto implements Value {
    private String id;
    private String name;
    private int ranking;
    private String serverId;

    public RoleDto(Role role){
        this.id = role.getId();
        this.name = role.getName();
        this.ranking = role.getRanking();
        this.serverId = role.getServerId();
    }

    @Override
    public String getAttribute(String key) {
        return switch (key){
            case "ranking" -> String.valueOf(this.ranking);
            case  "id" -> this.id;
            case "name" -> this.name;
            case "serverId" -> this.serverId;
            default -> null;
        } ;
    }

    @Override
    public void performAction(String action, Object... args) {

    }

    @Override
    public String getType() {
        return "role";
    }
}
