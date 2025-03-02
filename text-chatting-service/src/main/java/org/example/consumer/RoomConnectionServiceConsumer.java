package org.example.consumer;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.example.config.RabbitConfig;
import org.example.service.ChattingService;
import org.example.service.UserService;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListenerConfigurer;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerEndpoint;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistrar;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
public class RoomConnectionServiceConsumer implements RabbitListenerConfigurer {
    @Autowired
    UserService userService;


    @Autowired
    RabbitConfig rabbitConfig;

    @Autowired
    ChattingService chattingService;


    public void listen(Message message){
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            System.out.println(new String(message.getBody()));
            Map<String ,String> messageBody = objectMapper.readValue(new String(message.getBody()), new TypeReference<Map<String, String>>() {});
            if (userService.isMessageBrokerAuthorized(messageBody.get("Authorization")))
                if (messageBody.get("action").equals("delete-room-messages")){
                    System.out.println("deleting room messages");
                    String roomId = messageBody.get("roomId");
                    chattingService.deleteByRoomId(roomId);
                }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void configureRabbitListeners(RabbitListenerEndpointRegistrar rabbitListenerEndpointRegistrar) {
        SimpleRabbitListenerEndpoint endpoint = new SimpleRabbitListenerEndpoint();
        endpoint.setId(RabbitConfig.ROOM_TEXT_CHATTING_QUEUE);
        endpoint.setQueues(rabbitConfig.queue());

        endpoint.setAckMode(AcknowledgeMode.AUTO);
        endpoint.setMessageListener(
                this::listen
        );
        rabbitListenerEndpointRegistrar.registerEndpoint(endpoint);
    }
}























