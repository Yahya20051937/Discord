package org.example.consumer;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.discovery.converters.Auto;
import org.example.service.RoomManagementService;
import org.example.service.RoomPackageService;
import org.example.service.UserService;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.undertow.UndertowServletWebServer;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.SessionAttributes;

import java.util.Map;

@Component
public class ServerServiceConsumer {
    @Autowired
    UserService userService;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    RoomManagementService roomManagementService;

    @Autowired
    RoomPackageService roomPackageService;

    @RabbitListener(queues = "SERVER-ROOM-QUEUE")
    public void listen(Message message){
        try {
            Map<String, String> messageBody = objectMapper.readValue(new String(message.getBody()), new TypeReference<Map<String, String>>() {});
            System.out.println(messageBody);
            if (userService.isMessageBrokerAuthorized(messageBody.get("Authorization"))) {
                if ("delete-server".equals(messageBody.get("action"))) {
                    System.out.println("deleting server");
                    String serverId = messageBody.get("server-id");
                    roomManagementService.deleteServerRooms(serverId);
                    roomPackageService.deleteServerRoomPackages(serverId);
                }

                if ("create-default-roomPackages".equals(messageBody.get("action"))){
                    System.out.println("room packages defaults");
                    String serverId = messageBody.get("server-id");
                    roomPackageService.createDefaultsRoomPackages(serverId);
                }
            }


        } catch (Exception ignored){ignored.printStackTrace();}


    }
}
