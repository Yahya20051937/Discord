package org.example.dto.response.binary.node;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.example.dto.response.RoomDto;

@Data
@EqualsAndHashCode(callSuper = true)
public class RoomNode extends Node {

    public RoomNode(RoomDto roomDto){
        super.setValue(roomDto);
    }
}
