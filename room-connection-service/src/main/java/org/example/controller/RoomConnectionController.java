package org.example.controller;

import com.sun.source.tree.Tree;
import org.example.binary.tree.RoomConnectionsTree;
import org.example.dto.RoomDto;
import org.example.service.EndpointsService;
import org.example.service.RoomConnectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
public class RoomConnectionController {
    @Autowired
    RoomConnectionService roomConnectionService;

    @Autowired
    RoomConnectionsTree roomConnectionsTree;

    @Autowired
    EndpointsService endpointsService;

    @PostMapping("/api/room-connection/fill/room-connected-members")
    public List<RoomDto> fillRoomConnectedMember(@RequestBody List<RoomDto> rooms){
        roomConnectionService.fillRoomsConnectedMembers(rooms);
        return rooms;
    }

    @GetMapping("/api/room-connection/test/tree")
    public RoomConnectionsTree getRoomConnectionsTree(){
        return roomConnectionsTree;
    }

    @GetMapping("/api/room-connection/get-endpoints")
    public List<String> getRoomConnectionsServicesEndpoints(){
        return endpointsService.getEndpoints();
    }


}
