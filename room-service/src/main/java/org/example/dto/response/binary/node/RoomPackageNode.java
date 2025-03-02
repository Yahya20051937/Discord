package org.example.dto.response.binary.node;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.example.dto.response.RoomPackageDto;
import org.example.entity.RoomPackage;

@Data
@EqualsAndHashCode(callSuper = true)
public class RoomPackageNode extends Node {

    public RoomPackageNode(RoomPackageDto roomPackage){
        super.setValue(roomPackage);
    }
}
