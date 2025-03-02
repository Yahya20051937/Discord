package org.example.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.config.RabbitConfig;
import org.example.config.WebClientConfig;
import org.example.model.Value.RoomConnection;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Base64;
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


    public void notifyUserConnection(RoomConnection roomConnection) throws NoSuchAlgorithmException, InvalidKeySpecException {
        System.out.println("notifyUserConnection " + roomConnection.getRoomId());
        Map<String, String> body = Map.of(
                "action" , "server-notification",
                "type", "user-room-connection",
                "roomPackageId" , roomConnection.getRoomPackageId(),
                "username", roomConnection.getUsername(),
                "roomId" , roomConnection.getRoomId(),
                "serverId", roomConnection.getServerId(),
                "Authorization" , rabbitConfig.messagingToken(),
                "actor", roomConnection.getUsername()
        );
        this.notifyServer(body);

    }

    public void notifyUserDisconnection(RoomConnection roomConnection) throws NoSuchAlgorithmException, InvalidKeySpecException {
        System.out.println("notifyUserDisconnection " + roomConnection.getRoomId());
        Map<String, String> body = Map.of(
                "action" , "server-notification",
                "type", "user-room-disconnection",
                "roomPackageId" , roomConnection.getRoomPackageId(),
                "serverId" , roomConnection.getServerId(),
                "username", roomConnection.getUsername(),
                "roomId" , roomConnection.getRoomId(),
                "Authorization" , rabbitConfig.messagingToken(),
                "actor", roomConnection.getUsername()
        );
        this.notifyServer(body);

    }
}
