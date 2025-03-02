package org.example.dto.response;

import lombok.Data;
import org.example.RoomAccessType;
import org.example.RoomType;
import org.example.entity.Room;

@Data
public class RoomInfo {
    private String id;
    private String serverId;
    private RoomType roomType;
    private String roomPackageId;
    private RoomAccessType roomAccessType;

    public RoomInfo(Room room){
        this.id = room.getId();
        this.serverId = room.getServerId();
        this.roomType = room.getRoomType();
        this.roomAccessType = room.getRoomAccessType();
        this.roomPackageId = room.getRoomPackage().getId();
    }
}
