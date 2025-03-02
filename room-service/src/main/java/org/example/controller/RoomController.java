package org.example.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.example.dto.request.CreateRoomPackageRequest;
import org.example.dto.request.CreateRoomRequest;
import org.example.dto.response.RoomDto;
import org.example.dto.response.RoomInfo;
import org.example.dto.response.RoomPackageDto;
import org.example.dto.response.binary.tree.RoomPackagesTree;
import org.example.dto.response.binary.tree.RoomsTree;
import org.example.service.RoomManagementService;
import org.example.service.RoomPackageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class RoomController {
    @Autowired
    RoomManagementService roomManagementService;

    @Autowired
    RoomPackageService roomPackageService;


    @PostMapping("/api/room/auth/create")
    public ResponseEntity<?> create(@RequestBody CreateRoomRequest requestBody, HttpServletRequest request){
        return roomManagementService.createRoom(requestBody, request.getHeader("Authorization"));
    }

    @DeleteMapping("/api/room/auth/delete")
    public ResponseEntity<?> delete(@RequestParam("id") String roomId, HttpServletRequest request){
        return ResponseEntity.status(
                roomManagementService.deleteRoom(roomId, request.getHeader("Authorization"))
        ).build();
    }

    @GetMapping("/api/room/belongs-to-server")
    public Boolean doesRoomBelongToServer(@RequestParam("roomId") String roomId, @RequestParam("serverId") String serverId) {return roomManagementService.doesRoomBelongToServer(roomId, serverId);}

    @GetMapping("/api/room/getInfo")
    public RoomInfo getRoomInfo(@RequestParam("roomId") String roomId){
        return roomManagementService.getRoomInfo(roomId);
    }

    @PostMapping("/api/room/auth/create/room-package")
    public ResponseEntity<?> createRoomPackage(@RequestBody CreateRoomPackageRequest createRoomPackageRequest, HttpServletRequest request){
        return roomPackageService.createRoomPackage(createRoomPackageRequest, request.getHeader("Authorization"));
    }

    @DeleteMapping("/api/room/auth/delete/room-package")
    public ResponseEntity<?> deleteRoomPackage(@RequestParam String id, HttpServletRequest request){
        return ResponseEntity.status(roomPackageService.deleteRoomPackage(id, request.getHeader("Authorization")))
                .build();
    }

    @GetMapping("/api/room/get/server-room-packages")  // test
    public RoomPackagesTree getServerRoomsPackagesTree(@RequestParam String serverId){return roomPackageService.getServerRoomPackagesTree(serverId);}

    @GetMapping("/api/room/auth/get/serverRooms/{serverId}")
    public List<RoomsTree> getServerRoomsTrees(@PathVariable String serverId, HttpServletRequest request){  // test
        return roomManagementService.getSeversRoomsTrees(serverId, request.getHeader("Authorization"));
    }
}
