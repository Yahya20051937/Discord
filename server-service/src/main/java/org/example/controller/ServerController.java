package org.example.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.example.JoinQuit;
import org.example.dto.request.CreateRequest;
import org.example.dto.response.MemberShipDto;
import org.example.dto.response.ServerDto;
import org.example.entitiy.MemberShip;
import org.example.service.MediaService;
import org.example.service.MemberShipService;
import org.example.service.ServerService;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
public class ServerController {
    @Autowired
    ServerService serverService;

    @Autowired
    MemberShipService memberShipService;

    @Autowired
    MediaService mediaService;

    @GetMapping("/api/server/auth/get/joinedServers")
    public List<ServerDto> getUserJoinedServers(HttpServletRequest request){
        return memberShipService.getJoinedServers(request.getHeader("Authorization"));
    }

    @GetMapping("/api/server/search/{keyword}")
    public List<ServerDto> searchByKeyword(@PathVariable String keyword){
        return serverService.searchByKeyword(keyword);
    }

    @GetMapping("/api/server/get/serverMemberships/{serverId}")
    public List<MemberShipDto> getServerMemberships(@PathVariable String serverId){
        return memberShipService
                .memberShipRepository
                .findByServerId(serverId)
                .stream()
                .map(MemberShipDto::new)
                .toList();
    }

    @PostMapping("/api/server/auth/create")
    public ResponseEntity<?> create(@RequestBody CreateRequest createRequest, @NotNull HttpServletRequest request){
        System.out.println("Creating server");
        return serverService.create(createRequest, request.getHeader("Authorization"));

    }

    @DeleteMapping("/api/server/auth/delete")
    public ResponseEntity<?> delete(@RequestParam("id") String serverId, @NotNull HttpServletRequest request){
        HttpStatusCode status = serverService.delete(serverId, request.getHeader("Authorization"));
        return ResponseEntity.status(status)
                .build();
    }

    @GetMapping("/api/server/auth/does-server-belong-to-user")
    public Boolean doesServerBelongToUser(@RequestParam("id") String serverId, @NotNull HttpServletRequest request){
        return serverService.doesServerBelongsToUser(serverId, request.getHeader("Authorization"));
    }

    @GetMapping("/api/server/is-user-member-of-server")
    public Boolean isUserMemberOfServer(@RequestParam("serverId") String serverId, @RequestParam("username") String username){
        return memberShipService.isUserMemberOfServer(serverId, username);
    }


    @PutMapping("/api/server/auth/join")
    public ResponseEntity<?> join(@RequestParam("id") String serverId, @NotNull HttpServletRequest request){
        HttpStatusCode status = memberShipService.joinOrQuit(serverId, request.getHeader("Authorization"), JoinQuit.JOIN);
        return ResponseEntity.status(status)
                .build();
    }

    @PutMapping("/api/server/auth/quit")
    public ResponseEntity<?> quit(@RequestParam("id") String serverId, @NotNull HttpServletRequest request){
        HttpStatusCode status = memberShipService.joinOrQuit(serverId, request.getHeader("Authorization"), JoinQuit.QUIT);
        return ResponseEntity.status(status)
                .build();
    }

    @GetMapping("/api/server/is-present")
    public Boolean isPresent(@RequestParam("id") String serverId){
        return serverService.isPresent(serverId);
    }

    @GetMapping("/api/server/get-image")
    public Map<String, String> getServerImage(@RequestParam("id") String serverId){
        return mediaService.getServerImage(serverId);
    }
}
