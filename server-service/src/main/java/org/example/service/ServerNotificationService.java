package org.example.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.config.RabbitConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.config.RabbitConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
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

    public void  notifyServerJoin(String username, String serverId){
        try {
            this.notifyServer(
                    Map.of(
                            "action", "server-notification",
                            "type", "user_joined_server",
                            "serverId", serverId,
                            "username", username,
                            "Authorization", rabbitConfig.messagingToken(),
                            "actor", username
                    )
            );
        } catch (Exception e){
            e.printStackTrace();;
        }
    }

    public void notifyServerQuit(String username, String serverId){
        try {
            this.notifyServer(
                    Map.of(
                            "action", "server-notification",
                            "type", "user_quited_server",
                            "serverId", serverId,
                            "username", username,
                            "Authorization", rabbitConfig.messagingToken(),
                            "actor", username
                    )
            );
        } catch (Exception e){
            e.printStackTrace();;
        }
    }
}


