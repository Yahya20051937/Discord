package org.example.consumer;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.config.RabbitConfig;
import org.example.service.ServerConnectionService;
import org.example.service.UserService;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.annotation.RabbitListenerConfigurer;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerEndpoint;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistrar;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.awt.desktop.UserSessionEvent;
import java.io.IOException;
import java.util.Map;

@Component
public class ServerConnectionConsumer implements RabbitListenerConfigurer {

    @Autowired
    RabbitConfig rabbitConfig;

    @Autowired
    UserService userService;

    @Autowired
    ServerConnectionService serverConnectionService;

    public void listen(Message message){
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            Map<String ,String> messageBody = objectMapper.readValue(new String(message.getBody()), new TypeReference<Map<String, String>>() {});
            System.out.println(messageBody);
            if (userService.isMessageBrokerAuthorized(messageBody.get("Authorization")))
                if (messageBody.get("action").equals("server-notification")){
                    String serverId = messageBody.get("serverId");
                    String actor = messageBody.get("actor");
                    serverConnectionService.shareMessage(serverId, actor , messageBody);
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
