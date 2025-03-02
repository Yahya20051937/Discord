package org.example.service;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.example.AtomicResponse;
import org.example.AtomicStatus;
import org.example.RoomAccessType;
import org.example.RoomType;
import org.example.config.RabbitConfig;
import org.example.config.WebClientConfig;
import org.example.dto.request.CreateRoomRequest;
import org.example.dto.response.RoomDto;
import org.example.dto.response.RoomInfo;
import org.example.dto.response.binary.node.RoomNode;
import org.example.dto.response.binary.tree.RoomsTree;
import org.example.entity.Room;
import org.example.entity.RoomPackage;
import org.example.repository.RoomPackageRepository;
import org.example.repository.RoomRepository;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import java.nio.charset.StandardCharsets;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Service
public class RoomManagementService {
    @Autowired
    public RoomRepository repository;

    @Autowired
    RoomPackageRepository roomPackageRepository;

    @Autowired
    RoleService roleService;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    RabbitConfig rabbitConfig;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    WebClient webClient;

    @Autowired
    WebClientConfig webClientConfig;

    @Autowired
    ServerNotificationService serverNotificationService;

    @Autowired
    UserService userService;


    public ResponseEntity<?> createRoom(CreateRoomRequest roomRequest, String token){
        AtomicResponse atomicResponse = new AtomicResponse();
        roomPackageRepository.findById(roomRequest.getRoomPackageId())
                .ifPresentOrElse(
                        roomPackage -> {
                            boolean canUserCreateRoom = roleService.canUserCreateRoom(token, roomPackage.getServerId());
                            String username = userService.getUser(token).getUsername();
                            if (canUserCreateRoom){
                                repository.findByRoomPackage_ServerIdAndName(
                                        roomPackage.getServerId(),
                                        roomRequest.getName()
                                ).ifPresentOrElse(
                                        it -> atomicResponse.set(ResponseEntity.status(HttpStatus.CONFLICT).build()),
                                        () -> {
                                            Room room = new Room(roomRequest, roomPackage);
                                            repository.save(room);                                                               // null because the room was just created, it has no room scopes.
                                            serverNotificationService.notifyRoomCreation(new RoomDto(room), room.getServerId(), username);
                                            this.sendMessageToAddScopesToUserBiggestRoomPostCreation(room.getId(), username);
                                            atomicResponse.set(ResponseEntity.status(HttpStatus.CREATED).body(Map.of("roomId", room.getId())));
                                        }
                                );
                            }
		            else		
                            	atomicResponse.set(ResponseEntity.status(HttpStatus.FORBIDDEN).build());
                        }
                        ,
                        () -> atomicResponse.set(ResponseEntity.status(404).build())
                );
        return atomicResponse.get();
    }



    public HttpStatusCode deleteRoom(String roomId, String token){
        AtomicStatus status = new AtomicStatus();
        String username = userService.getUser(token).getUsername();
        repository.findById(roomId)
                .ifPresentOrElse(
                        it -> {
                            if (roleService.canUserDeleteRoom(token, it)){
                                this.processRoomDeletion(it);
                                serverNotificationService.notifyRoomDeletion(it, it.getServerId(), username);
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

    public Boolean doesRoomBelongToServer(String roomId, String serverId){
        AtomicBoolean yes = new AtomicBoolean(false);
        repository.findById(roomId).ifPresent(
                it -> yes.set(it.getServerId().equals(serverId))
        );
        return yes.get();
    }

    public void deleteServerRooms(String serverId){
        repository.findByRoomPackage_ServerId(serverId)
                .forEach(
                        this::processRoomDeletion
                );
    }

    private void processRoomDeletion(Room room){  // i delete room manually instead of relying on the relationship because i need to send some messages here.
        this.sendMessageToDeleteRoomScopes(room.getId());
        this.sendMessageToDeleteRoomMessages(room.getId());
        this.sendMessageToDisconnectFromDeletedRoom(room.getId());
        repository.delete(room);
    }



    public void sendMessageToDeleteRoomScopes(String roomId){
        try {
            Map<String, String> messageBody = Map.of(
                    "Authorization", rabbitConfig.messagingToken(),
                    "room-id", roomId,
                    "action", "delete-room"
            );


            rabbitTemplate.send(
                    RabbitConfig.ROOM_ROLE_EXCHANGE,
                    RabbitConfig.ROOM_ROLE_ROUTING_KEY,
                    new Message(
                            objectMapper.writeValueAsString(messageBody).getBytes(StandardCharsets.UTF_8)
                    )
            );
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void sendMessageToAddScopesToUserBiggestRoomPostCreation(String roomId, String username){
        // after creating the room,  we must give privileges to the user who created it, so we add all possible scopes to user hightest role.
        try {
            Map<String, String> messageBody = Map.of(
                    "Authorization", rabbitConfig.messagingToken(),
                    "room-id", roomId,
                    "username", username,
                    "action", "add-scopes-in-room-to-user-highest-role"
            );


            rabbitTemplate.send(
                    RabbitConfig.ROOM_ROLE_EXCHANGE,
                    RabbitConfig.ROOM_ROLE_ROUTING_KEY,
                    new Message(
                            objectMapper.writeValueAsString(messageBody).getBytes(StandardCharsets.UTF_8)
                    )
            );
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void sendMessageToDisconnectFromDeletedRoom(String roomId){ // not tested.
        try {
            Map<String, String> messageBody = Map.of(
                    "Authorization", rabbitConfig.messagingToken(),
                    "roomId", roomId,
                    "action", "disconnect-from-room"
            );
            for (String queueName : this.getAllRoomConnectionsQueues()){
                String id = queueName.split("/")[2];
                String exchange = "room-connections/exchange/" + id;
                String routingKey = "room-connections/routing-key/" + id;
                rabbitTemplate.convertAndSend(
                        exchange,
                        routingKey,
                        new org.springframework.amqp.core.Message(objectMapper.writeValueAsString(messageBody).getBytes(StandardCharsets.UTF_8)),
                        m -> {
                            m.getMessageProperties().setContentType("application/json");
                            return m;
                        }
                );
            }

        } catch (Exception e){
            e.printStackTrace();
        }

    }


    public RoomInfo getRoomInfo(String roomId){
        return repository
                .findById(roomId)
                .map(RoomInfo::new)
                .orElse(null);
    }

    public List<RoomsTree> getSeversRoomsTrees(String serverId, String token){ // test
        List<RoomsTree> serverRoomsTrees = new ArrayList<>();
        roomPackageRepository.findByServerId(serverId)
                .forEach(
                        it -> {
                            try {
                                serverRoomsTrees.add(this.getRoomPackageRoomsWithClientScopesAndVocalRoomsConnectedMembers(it.getId(), token));
                            } catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                );
        return serverRoomsTrees;
    }

    private RoomsTree getRoomPackageRoomsWithClientScopesAndVocalRoomsConnectedMembers(String roomPackageId, String token){
        /*Here we retrieve the package rooms by id, then we make a post request to get the client scopes for each room, when we make another post request to get the connected room
        * members for each vocal room.*/
        List<RoomDto> rooms =  this.getRoomsConnectedMembers(
                this.getRoomsClientScopes(
                        repository.findByRoomPackageId(roomPackageId)
                                .stream()
                                .map(RoomDto::new)
                                .toList(),
                        token
            )
        );
        RoomsTree roomsTree = new RoomsTree();
        for (RoomDto room : rooms)
            roomsTree.insert(new RoomNode(room));
        return roomsTree;

    }

    private List<RoomDto> getRoomsClientScopes(List<RoomDto> rooms, String token){
        return webClient.post()
                .uri("http://role-service/api/role/auth/get/client-rooms-scopes")
                .header("Authorization", token)
                .bodyValue(Map.of("rooms", rooms))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<RoomDto>>() {})
                .block();
    }

    private List<String> getRoomConnectionServicesEndpoints(){
        return webClient.get()
                .uri("http://room-connection-service/api/room-connection/get-endpoints")
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<String>>() {})
                .block();
    }

    private List<RoomDto> getRoomsConnectedMembers(List<RoomDto> rooms){ // for each room-connection-service-application we get the connected member in each room.
        for (String endpoint : this.getRoomConnectionServicesEndpoints())
            try {
                List<RoomDto> response = webClientConfig.externalClient().post()
                        .uri("http://" + endpoint +  "/api/room-connection/fill/room-connected-members")
                        .bodyValue(rooms)
                        .retrieve()
                        .bodyToMono(new ParameterizedTypeReference<List<RoomDto>>() { })
                        .block();
                rooms = (response != null ) ? response : rooms;
            } catch (Exception e) {
                e.printStackTrace();
            }

        return rooms;
    }

    public void sendMessageToDeleteRoomMessages(String roomId){
        try{
            Map<String, String> messageBody = Map.of(
                    "Authorization", rabbitConfig.messagingToken(),
                    "roomId", roomId,
                    "action", "delete-room-messages"
            );
            rabbitTemplate.convertAndSend(
                    RabbitConfig.ROOM_TEXT_CHATTING_EXCHANGE,
                    RabbitConfig.ROOM_TEXT_CHATTING_ROUTING_KEY,
                    new org.springframework.amqp.core.Message(objectMapper.writeValueAsString(messageBody).getBytes(StandardCharsets.UTF_8)),
                    m -> {
                        m.getMessageProperties().setContentType("application/json");
                        return m;
                    }
            );

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private List<String> getAllRoomConnectionsQueues(){
        return rabbitConfig.getAllQueues((String name, String key) -> name.split("/")[0].equals(key), "room-connections");
    }

    public void createDefaultRooms(RoomPackage roomPackage, RoomType roomType){
        List<String> roomNames = (roomType.equals(RoomType.VOCAL)) ? List.of("vocal room 1", "vocal room 2") : List.of("text room 1", "text room 2");

        Room room1 = Room.builder()
                .id(UUID.randomUUID().toString())
                .name(roomNames.get(0))
                .roomAccessType(RoomAccessType.PUBLIC)
                .roomType(roomType)
                .roomPackage(roomPackage)
                .build();

        Room room2 = Room.builder()
                .id(UUID.randomUUID().toString())
                .name(roomNames.get(1))
                .roomAccessType(RoomAccessType.PUBLIC)
                .roomType(roomType)
                .roomPackage(roomPackage)
                .build();

        repository.save(room1);
        repository.save(room2);
    }


}

























