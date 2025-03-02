package org.example.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.RoomAccessType;
import org.example.RoomType;
import org.example.ScopeType;
import org.example.service.RoomScopeService;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoomDto implements Value {
    private String id;
    private String serverId;
    private String name;
    private List<ScopeType> clientScopes;
    private List<String> connectedMembers;
    private RoomType roomType;
    private String type;
    private RoomAccessType roomAccessType;
    private String roomPackageId;
    private Integer mainRoleRanking;

    @Override
    public String getAttribute(String key) {
        return switch (key){
            case "id" -> this.id;
            case "roomPackageId" -> this.roomPackageId;
            default -> "";
        };
    }

    @Override
    public void performAction(String action, Object... args) {
        if (action.equals("update-client-scopes")){
            RoomScopeService roomScopeService = (RoomScopeService) args[0];
            String token = (String) args[1];
            this.setClientScopes(roomScopeService.getRoomClientScopes(this.id, token));
        }
    }
}
