package org.example.consumer;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.service.RoomScopeService;
import org.example.service.UserService;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class RoomServiceConsumer {
    @Autowired
    RoomScopeService roomScopeService;

    @Autowired
    UserService userService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @RabbitListener(queues = "ROOM-ROLE-QUEUE")
    public void receiveMessage(Message message){
        try {
            Map<String, String> messageBody = objectMapper.readValue(new String(message.getBody()), new TypeReference<Map<String, String>>() {});
            if (userService.isMessageBrokerAuthorized(messageBody.get("Authorization"))) {
                if ("delete-room".equals(messageBody.get("action"))) {
                    System.out.println("deleting room");
                    String roomId = messageBody.get("room-id");
                    if (roomId != null)
                        roomScopeService.deleteRoomScopes(roomId);
                }
                else if ("add-scopes-in-room-to-user-highest-role".equals(messageBody.get("action"))) {
                    String roomId = messageBody.get("room-id");
                    String username = messageBody.get("username");
                    roomScopeService.addScopesToRoomForUserBiggestRole(roomId, username);
                }
            }


        } catch (Exception ignored){ignored.printStackTrace();}
    }
}
