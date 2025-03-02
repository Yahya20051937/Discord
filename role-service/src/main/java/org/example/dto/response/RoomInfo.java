package org.example.dto.response;

import lombok.Data;
import org.example.RoomAccessType;
import org.example.RoomType;

@Data
public class RoomInfo {
    private String id;
    private String serverId;
    private RoomType roomType;
    private RoomAccessType roomAccessType;
    private String roomPackageId;



}
