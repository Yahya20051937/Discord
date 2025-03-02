package org.example.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NoArgsConstructor;import org.example.RoomAccessType;
import org.example.RoomType;

@Data

@NoArgsConstructor
@AllArgsConstructor
public class CreateRoomRequest {
    private String name;
    private String roomPackageId;
    private RoomAccessType roomAccessType;
    private RoomType roomType;
}
