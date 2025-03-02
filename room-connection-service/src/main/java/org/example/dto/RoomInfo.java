package org.example.dto;

import lombok.Data;
import org.example.RoomAccessType;
import org.example.RoomType;

@Data
public class RoomInfo {
    private String id;
    private String serverId;
    private String roomPackageId;
    private RoomType roomType;
    private RoomAccessType roomAccessType;
}
