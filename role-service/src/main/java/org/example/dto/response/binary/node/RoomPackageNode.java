package org.example.dto.response.binary.node;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.example.dto.response.RoomPackageDto;

@Data
@EqualsAndHashCode(callSuper = true)
public class RoomPackageNode extends Node {

    public RoomPackageNode(RoomPackageDto roomPackage){
        super.setValue(roomPackage);
    }
}
