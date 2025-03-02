package org.example.service;

import jakarta.transaction.Transactional;
import org.example.AtomicResponse;
import org.example.AtomicStatus;
import org.example.RoomType;
import org.example.dto.request.CreateRoomPackageRequest;
import org.example.dto.response.RoomDto;
import org.example.dto.response.RoomPackageDto;
import org.example.dto.response.binary.tree.RoomPackagesTree;
import org.example.entity.Room;
import org.example.entity.RoomPackage;
import org.example.repository.RoomPackageRepository;
import org.example.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class RoomPackageService {
    @Autowired
    RoomPackageRepository repository;

    @Autowired
    RoomRepository roomRepository;

    @Autowired
    ServerNotificationService serverNotificationService;

    @Autowired
    RoleService roleService;

    @Autowired
    RoomManagementService roomManagementService;

    @Autowired
    UserService userService;

    public ResponseEntity<?> createRoomPackage(CreateRoomPackageRequest createRoomPackageRequest, String token){
        AtomicResponse atomicResponse = new AtomicResponse();
        if (roleService.canUserCreateRoom(token, createRoomPackageRequest.getServerId())) {// this also checks if the server is present
            repository.findByServerIdAndName(createRoomPackageRequest.getServerId(), createRoomPackageRequest.getName())
                    .ifPresentOrElse(
                            roomPackage -> atomicResponse.set(ResponseEntity.status(HttpStatus.CONFLICT).build())
                            ,
                            () -> {
                                String username = userService.getUser(token).getUsername();
                                RoomPackage roomPackage = new RoomPackage(createRoomPackageRequest);
                                repository.save(roomPackage);
                                serverNotificationService.notifyRoomPackageCreation(new RoomPackageDto(roomPackage), username);
                                atomicResponse.set(ResponseEntity.status(201).body(Map.of("roomPackageId", roomPackage.getId())));
                            }
                    );
            return atomicResponse.get();
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .build();
    }

    public HttpStatusCode deleteRoomPackage(String id, String token){
        AtomicStatus status = new AtomicStatus();
        repository.findById(id)
                .ifPresentOrElse(
                        it -> {
                            if (roleService.canUserDeleteRooms(
                                    token,
                                    it.getRooms().stream().map(Room::getId).toList(),
                                    it.getServerId()
                            )){
                                String username = userService.getUser(token).getUsername();
                                repository.delete(it);
                                serverNotificationService.notifyRoomPackageDeletion(it.getServerId(), it.getId(), username);
                                status.set(HttpStatus.OK);
                            }
                            else
                                status.set(HttpStatus.FORBIDDEN);
                        }
                        ,
                        () -> status.set(HttpStatus.NOT_FOUND)
                );
        return status.get();
    }

    public void createDefaultsRoomPackages(String serverId){
        RoomPackage roomPackage1 = RoomPackage.builder()
                .id(UUID.randomUUID().toString())
                .name("VOCAL-ROOMS")
                .serverId(serverId)
                .build();

        RoomPackage roomPackage2 = RoomPackage.builder()
                .id(UUID.randomUUID().toString())
                .name("TEXT-ROOMS")
                .serverId(serverId)
                .build();

        repository.save(roomPackage1);
        repository.save(roomPackage2);

        roomManagementService.createDefaultRooms(roomPackage1, RoomType.VOCAL);
        roomManagementService.createDefaultRooms(roomPackage2, RoomType.TEXTUAL);

    }

    @Transactional
    public void deleteServerRoomPackages(String serverId){
        repository.deleteByServerId(serverId);
    }

    public RoomPackagesTree getServerRoomPackagesTree(String serverId){
        List<RoomPackageDto> roomPackages =  repository.findByServerId(serverId)
                .stream()
                .map(RoomPackageDto::new)
                .toList();
        return new RoomPackagesTree(roomPackages);
    }

}








































