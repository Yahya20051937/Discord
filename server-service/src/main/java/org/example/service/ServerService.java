package org.example.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.config.RabbitConfig;
import org.example.dto.request.CreateRequest;
import org.example.dto.response.ServerDto;
import org.example.entitiy.MemberShip;
import org.example.entitiy.Server;
import org.example.repository.MemberShipRepository;
import org.example.repository.ServerRepository;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class ServerService {
    @Autowired
    ServerRepository repository;

    @Autowired
    UserService userService;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    RabbitConfig rabbitConfig;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    MemberShipRepository memberShipRepository;

    @Autowired
    MediaService mediaService;

    public List<ServerDto> searchByKeyword(String keyword){
        return repository.findByNameContaining(keyword)
                .stream()
                .map(ServerDto::new)
                .toList();
    }

    public ResponseEntity<?> create(CreateRequest createRequest, String token){
        String username = userService.getUser(token).getUsername();
        Server server = new Server(createRequest, username);
        if (mediaService.saveImage(createRequest, server.getId())) {
            repository.save(server);
            memberShipRepository.save(new MemberShip(username, server));
            this.sendMessageToCreateAndAssignServerOwnerAndMemberRole(server.getId(), username);
            this.sendMessageToCreateDefaultRoomPackages(server.getId());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("serverId", server.getId()));
        }
        return ResponseEntity.internalServerError()
                .build();
    }

    public HttpStatus delete(String serverId, String token){
        AtomicBoolean isPresent = new AtomicBoolean(false);
        AtomicBoolean isServerOwner = new AtomicBoolean(false);
        repository.findById(serverId)
                .ifPresent(
                        it  -> {
                            isPresent.set(true);
                            String username = userService.getUser(token).getUsername();
                            if (username.equals(it.getOwner())) {
                                isServerOwner.set(true);
                                repository.delete(it); // memberships will be deleted automatically
                                mediaService.deleteImage(it.getId());
                                this.sendMessageToDeleteServerChildren(serverId);

                            }
                        }
                );
        if (isPresent.get())
            if (isServerOwner.get())
                return HttpStatus.OK;
            else
                return HttpStatus.FORBIDDEN;
        return HttpStatus.NOT_FOUND;
    }

    public Boolean doesServerBelongsToUser(String serverId, String token){
        String username = userService.getUser(token).getUsername();
        AtomicBoolean yes = new AtomicBoolean(false);
        repository.findById(serverId)
                .ifPresent(
                        it -> {
                            if (it.getOwner().equals(username))
                                yes.set(true);
                        }
                );
        return yes.get();
    }

    public void sendMessageToDeleteServerChildren(String serverId){
        try {
            Map<String, String> messageBody = Map.of(
                    "Authorization", rabbitConfig.messagingToken(),
                    "action", "delete-server",
                    "server-id", serverId
            );
            byte[] messageBodyBytes = objectMapper.writeValueAsString(messageBody).getBytes(StandardCharsets.UTF_8);
            rabbitTemplate.send(
                    RabbitConfig.SERVER_ROLE_EXCHANGE,  // this will delete all associated roles
                    RabbitConfig.SERVER_ROLE_ROUTING_KEY,
                    new Message(messageBodyBytes)
            );

            rabbitTemplate.send(
                    RabbitConfig.SERVER_ROOM_EXCHANGE,
                    RabbitConfig.SERVER_ROOM_ROUTING_KEY,   // this will delete all associated rooms, and thus all associated room scopes
                    new Message(messageBodyBytes)
            );
        } catch (Exception e){
            e.printStackTrace();
        }

    }

    private void sendMessageToCreateAndAssignServerOwnerAndMemberRole(String serverId,String owner){
        try {
            Map<String, String> messageBody = Map.of(
                    "Authorization", rabbitConfig.messagingToken(),
                    "action", "create&assign-owner&member-role",
                    "server-id", serverId,
                    "server-owner", owner
            );
            rabbitTemplate.send(
                    RabbitConfig.SERVER_ROLE_EXCHANGE,
                    RabbitConfig.SERVER_ROLE_ROUTING_KEY,
                    new Message(objectMapper.writeValueAsString(messageBody).getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e){
            e.printStackTrace();
        }
    }



    private void sendMessageToCreateDefaultRoomPackages(String serverId){
        try {
            Map<String, String> messageBody = Map.of(
                    "Authorization", rabbitConfig.messagingToken(),
                    "action","create-default-roomPackages",
                    "server-id", serverId
            );
            rabbitTemplate.send(
                    RabbitConfig.SERVER_ROOM_EXCHANGE,
                    RabbitConfig.SERVER_ROOM_ROUTING_KEY,
                    new Message(objectMapper.writeValueAsString(messageBody).getBytes(StandardCharsets.UTF_8))
            );
        } catch (Exception e){
            e.printStackTrace();
        }

    }

    public Boolean isPresent(String serverId){
        return repository.findById(serverId)
                .isPresent();
    }
}



















