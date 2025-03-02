package org.example.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.RoomType;
import org.example.RoomAccessType;
import org.example.ScopeType;
import org.example.entity.Room;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoomDto implements Value {
    private String id;
    private String name;
    private String serverId;
    private RoomType roomType;
    private RoomAccessType roomAccessType; 
    private String roomPackageId;
	

    private List<ScopeType> clientScopes;
    private List<String> connectedMembers;
    private Integer mainRoleRanking;

    public RoomDto(Room room){
        this.id = room.getId();
        this.serverId = room.getServerId();
        this.roomType = room.getRoomType();
        this.clientScopes = new ArrayList<>();
        this.connectedMembers = new ArrayList<>();
        this.name = room.getName();
	    this.roomAccessType = room.getRoomAccessType();
	    this.roomPackageId = room.getRoomPackage().getId();
        this.mainRoleRanking = null;
    }

    @Override
    public String getAttribute(String key) {
        return switch (key){
            case "id" -> this.id;
            case "serverId" -> this.serverId;
            case "name" -> this.name;
            default -> "";
        };
    }

    @Override
    public void performAction(String action, Object... args) {

    }

    @Override
    public String getType() {
        return "room";
    }
}
