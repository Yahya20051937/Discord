package org.example.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.MediaType;
import org.example.ScopeType;
import org.example.config.RabbitConfig;
import org.example.config.WebClientConfig;
import org.example.dto.MessageDto;
import org.example.dto.RoomInfo;
import org.example.dto.SendMessageRequest;
import org.example.entity.Message;
import org.example.repository.MessageRepository;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.*;

@Service
public class ChattingService {
    @Autowired
    RabbitAdmin rabbitAdmin;

    @Autowired
    RoomScopeService roomScopeService;

    @Autowired
    MessageRepository messageRepository;

    @Autowired
    UserService userService;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    RabbitConfig rabbitConfig;

    @Autowired
    ServerNotificationService serverNotificationService;

    @Autowired
    WebClient webClient;

    @Autowired
    MediaService mediaService;

    public ResponseEntity<?> getLastNRoomMessagesBeforeX(int n , long x, String roomId, String token){
        if (roomScopeService.doesUserScope(roomId, ScopeType.READ, token)){
            long roomMessagesCount = messageRepository.countByRoomId(roomId);
            long a = x- n;
            long b = x;
            List<MessageDto> messages = messageRepository.findLastNMessagesBeforeX(a, b, roomId)
                    .stream()
                    .map(MessageDto::new)
                    .toList();
            return ResponseEntity.ok(messages);
        }
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .build();

    }



    public ResponseEntity<?> sendMessage(SendMessageRequest request, String token){
        String username = userService.getUser(token).getUsername();
        if (roomScopeService.doesUserScope(request.getRoomId(), ScopeType.WRITE, token)){
            Message message = new Message(request, username, messageRepository.countByRoomId(request.getRoomId()) + 1);
            messageRepository.save(message);
            if (!message.getMediaType().equals(MediaType.NONE))
                mediaService.createEmptyFile(message.getId(), message.getMediaType());
            try {
                this.shareMessage(message);
                RoomInfo room = this.getRoomInfo(request.getRoomId());
                serverNotificationService.notifyMessageSent(username, message.getRoomId(), room.getRoomPackageId(), room.getServerId());
            } catch (NoSuchAlgorithmException | InvalidKeySpecException | JsonProcessingException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }

            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("id", message.getId());
            responseBody.put("counter", String.valueOf(message.getCounter()));
            if (message.getMediaType() != MediaType.NONE)
                try {
                    responseBody.put("uploadToken", userService.generateUploadMessageMediaToken(message.getId()));
                } catch (Exception e){
                    e.printStackTrace();
                }

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(responseBody);
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .build();

    }

    private void shareMessage(Message message) throws JsonProcessingException, NoSuchAlgorithmException, InvalidKeySpecException {
        // this to share message to clients connected to the room.
        Map<String, String> body  = Map.of(
                "message", objectMapper.writeValueAsString(new MessageDto(message)),
                "Authorization", rabbitConfig.messagingToken(),
                "action", "share-message"
        );
        for (String queueName : this.getAllRoomConnectionsQueues())
            try {
                String id = queueName.split("/")[2];
                String exchange = "room-connections/exchange/" + id;
                String routingKey = "room-connections/routing-key/" + id;
                rabbitTemplate.convertAndSend(
                        exchange,
                        routingKey,
                        new org.springframework.amqp.core.Message(objectMapper.writeValueAsString(body).getBytes(StandardCharsets.UTF_8)),
                        m -> {
                            m.getMessageProperties().setContentType("application/json");
                            return m;
                        }
                );
                System.out.println("Message to share sent to queue " + queueName);
                System.out.println(exchange);
                System.out.println(routingKey);
            } catch (IndexOutOfBoundsException | JsonProcessingException e){
                e.printStackTrace();
            }

    }

    private List<String> getAllRoomConnectionsQueues(){
        return rabbitConfig.getAllQueues((String name, String key) -> name.split("/")[0].equals(key), "room-connections");
    }

    @Transactional
    public void deleteByRoomId(String roomId){
        messageRepository.deleteByRoomId(roomId);
    }

    public Long getRoomMessagesCount(String roomId){
        return messageRepository.countByRoomId(roomId);
    }

    public void handleUploadFailure(String msgId, String token){
        String username = userService.getUser(token).getUsername();
        messageRepository.findById(msgId)
                .ifPresent(it -> {
                    if (it.getWriter().equals(username))
                        if (it.getMediaType() != MediaType.NONE) {
                            try {
                                mediaService.clearMediaFile(it.getId(), it.getMediaType());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    messageRepository.delete(it);
                });

    }

    private RoomInfo getRoomInfo(String roomId){
        return webClient.get()
                .uri("http://room-service/api/room/getInfo?roomId=" + roomId)
                .retrieve()
                .bodyToMono(RoomInfo.class)
                .block();
    }




}
