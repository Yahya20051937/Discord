package org.example.dto.response;

import lombok.Data;
import org.example.dto.response.binary.tree.RoomsTree;
import org.example.service.RoomScopeService;

@Data
public class RoomPackageDto implements Value {
    private String id;
    private String name;
    private String serverId;
    private RoomsTree roomsTree;





    @Override
    public String getAttribute(String key) {
        return switch (key){
            case "id" -> this.id;
            case "serverId" -> this.serverId;
            default -> "";
        };
    }

    @Override
    public void performAction(String action, Object... args) {
        if (action.equals("update-client-scopes")){
            RoomScopeService roomScopeService = (RoomScopeService) args[0];
            String token = (String) args[1];
            roomsTree.performToAll(action, roomScopeService, token);
        }
    }

    @Override
    public String getType() {
        return null;
    }
}
