package org.example.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.RoomType;
import org.example.binary.tree.RoomConnectionsTree;
import org.example.config.RabbitConfig;
import org.example.service.*;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.annotation.RabbitListenerConfigurer;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerEndpoint;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistrar;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.lang.reflect.Type;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Map;

@Component
public class RoomConnectionServiceConsumer implements RabbitListenerConfigurer {
    @Autowired
    UserService userService;
    @Autowired
    RoomConnectionService roomConnectionService;

    @Autowired
    RabbitConfig rabbitConfig;

    @Autowired
    RoomConnectionsTree roomConnectionsTree;
    @Autowired
    private ServerNotificationService serverNotificationService;

    @Autowired
    EndpointsService endpointsService;


    public void listen(Message message){
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            Map<String ,String> messageBody = objectMapper.readValue(new String(message.getBody()), new TypeReference<Map<String, String>>() {});
            if (userService.isMessageBrokerAuthorized(messageBody.get("Authorization")))
                if (messageBody.get("action").equals("share-message")){
                    System.out.println("sharing message");
                    String messageJson = messageBody.get("message");
                    org.example.model.Message messageContent = objectMapper.readValue(messageJson, new TypeReference<org.example.model.Message>() {});
                    roomConnectionService.shareMessage(messageContent);
                }

                else if (messageBody.get("action").equals("disconnect-from-room")) {
                    System.out.println("disconnecting from room");
                    String roomId = messageBody.get("roomId");
                    roomConnectionService.disconnectFromRoomAndDelete(roomId);
                }

                else if (messageBody.get("action").equals("disconnect-user-from-room")) {
                    String roomId = messageBody.get("roomId");
                    String username = messageBody.get("username");
                    roomConnectionService.disconnectUserManuallyFromRoom(roomId, username);
                    //roomConnectionsTree.deleteAndDisconnectByRoomIdAndUsername(roomId, username, serverNotificationService);

                }

                /*else if (messageBody.get("action").equals("delete-endpoint")) {
                    String endpoint = messageBody.get("endpoint");
                    endpointsService.deleteEndpoint(endpoint);
                }*/

                else if (messageBody.get("action").equals("share-vocal-message")) {
                    String messageJson = messageBody.get("message");
                    org.example.model.VocalMessage messageContent = objectMapper.readValue(messageJson, new TypeReference<>() {});
                    roomConnectionService.shareMessage(messageContent);
                }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void configureRabbitListeners(RabbitListenerEndpointRegistrar rabbitListenerEndpointRegistrar) {
        SimpleRabbitListenerEndpoint endpoint = new SimpleRabbitListenerEndpoint();
        endpoint.setId(rabbitConfig.queueId);
        endpoint.setQueues(rabbitConfig.queue());

        endpoint.setAckMode(AcknowledgeMode.AUTO);
        endpoint.setMessageListener(
                this::listen
        );
        rabbitListenerEndpointRegistrar.registerEndpoint(endpoint);
    }
}























