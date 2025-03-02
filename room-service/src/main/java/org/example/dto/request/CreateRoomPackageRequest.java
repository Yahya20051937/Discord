package org.example.dto.request;

import lombok.Data;

@Data
public class CreateRoomPackageRequest {
    private String name;
    private String serverId;
}
