package org.example.model.Value;

import lombok.Data;
import org.example.RoomType;

@Data
public class UserConnection implements Value {
    private String username;
    private RoomType roomType;
    private String roomId;

    public UserConnection(RoomConnection roomConnection){
        this.username = roomConnection.getUsername();
        this.roomType = roomConnection.getRoomType();
        this.roomId = roomConnection.getRoomId();
    }

    @Override
    public String getAttribute(String key) {
        return switch (key) {
            case "username" -> this.getUsername();
            case "roomType" -> this.roomType.name();
            case "roomId" -> this.roomId;
            default -> null;
        };
    }

    @Override
    public void performAction(String action,Object...args){}
}
