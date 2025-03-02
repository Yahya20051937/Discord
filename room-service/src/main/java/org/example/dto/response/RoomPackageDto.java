package org.example.dto.response;

import lombok.Data;
import org.example.dto.response.RoomDto;
import org.example.dto.response.Value;
import org.example.dto.response.binary.node.RoomNode;
import org.example.dto.response.binary.tree.RoomsTree;
import org.example.entity.RoomPackage;

import java.util.List;

@Data
public class RoomPackageDto implements Value {
    private String id;
    private String name;
    private String serverId;

    public RoomPackageDto(RoomPackage roomPackage){
        this.id = roomPackage.getId();
        this.name = roomPackage.getName();
        this.serverId = roomPackage.getServerId();
    }

    @Override
    public String getAttribute(String key) {
        return switch (key){
            case "id" -> this.id;
            case "serverId" -> this.serverId;
            default -> "";
        };
    }

    @Override
    public void performAction(String action, Object... args) {

    }

    @Override
    public String getType() {
        return "roomPackage";
    }
}
