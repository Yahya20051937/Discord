package org.example.dto.response.binary.tree;


import lombok.Data;
import lombok.EqualsAndHashCode;
import org.example.dto.response.RoomDto;
import org.example.dto.response.RoomPackageDto;
import org.example.dto.response.binary.node.RoomPackageNode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class RoomPackagesTree extends Tree {
    {
        super.setMainAttribute("id");
    }

    public RoomPackagesTree(List<RoomPackageDto> roomPackages){
        for (RoomPackageDto roomPackage : roomPackages){
            super.insert(new RoomPackageNode(roomPackage));
        }
    }
}

