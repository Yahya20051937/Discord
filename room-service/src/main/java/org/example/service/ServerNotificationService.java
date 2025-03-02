package org.example.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.config.RabbitConfig;
import org.example.dto.response.RoomDto;
import org.example.dto.response.RoomPackageDto;
import org.example.entity.Room;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.List;
import java.util.Map;

@Service
public class ServerNotificationService {
    @Autowired
    RabbitConfig rabbitConfig;

    @Autowired
    RabbitTemplate rabbitTemplate;
    @Autowired
    ObjectMapper objectMapper;


    private List<String> getAllServerConnectionsQueues(){
        return rabbitConfig.getAllQueues((String name, String key) -> name.split("/")[0].equals(key), "server-connections");
    }

    private void  notifyServer(Map<String, String> body){
        for (String queueName : this.getAllServerConnectionsQueues())
            try {
                String id = queueName.split("/")[2];
                String exchange = "server-connections/exchange/" + id;
                String routingKey = "server-connections/routing-key/" + id;
                rabbitTemplate.convertAndSend(
                        exchange,
                        routingKey,
                        new org.springframework.amqp.core.Message(objectMapper.writeValueAsString(body).getBytes(StandardCharsets.UTF_8)),
                        m -> {
                            m.getMessageProperties().setContentType("application/json");
                            return m;
                        }
                );
            } catch (Exception e){
                e.printStackTrace();;
            }
    }

    public void notifyRoomCreation(RoomDto roomDto, String serverId, String actor){
        System.out.println(serverId);
        try {
            this.notifyServer(
                    Map.of(
                            "action", "server-notification",
                            "type", "room_created",
                            "serverId", serverId,
                            "room", objectMapper.writeValueAsString(roomDto),
                            "actor", actor,
                            "Authorization", rabbitConfig.messagingToken()

                    )
            );
        }catch (NoSuchAlgorithmException | InvalidKeySpecException | JsonProcessingException e){
            e.printStackTrace();
        }


    }

    public void notifyRoomDeletion(Room room, String serverId, String actor){
        try {
            this.notifyServer(
                    Map.of(
                            "action", "server-notification",
                            "type", "room_deleted",
                            "serverId", serverId,
                            "room", objectMapper.writeValueAsString(new RoomDto(room)),
                            "actor", actor,
                            "Authorization", rabbitConfig.messagingToken()
                    )
            );
        } catch (NoSuchAlgorithmException | InvalidKeySpecException | JsonProcessingException e){
            e.printStackTrace();
        }

    }


    public void notifyRoomPackageCreation(RoomPackageDto roomPackageDto, String actor){
        try {
            this.notifyServer(
                    Map.of(
                            "action", "server-notification",
                            "type", "room_package_created",
                            "serverId", roomPackageDto.getServerId(),
                            "roomPackage", objectMapper.writeValueAsString(roomPackageDto),
                            "actor", actor,
                            "Authorization", rabbitConfig.messagingToken()

                    )
            );
        }catch (NoSuchAlgorithmException | InvalidKeySpecException | JsonProcessingException e){
            e.printStackTrace();
        }
    }

    public void notifyRoomPackageDeletion(String serverId, String roomPackageId,String actor){
        try {
            this.notifyServer(
                    Map.of(
                            "action", "server-notification",
                            "type", "room_package_deleted",
                            "serverId", serverId,
                            "roomPackageId", roomPackageId,
                            "actor", actor,
                            "Authorization", rabbitConfig.messagingToken()
                    )
            );
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e){
            e.printStackTrace();
        }
    }
}
