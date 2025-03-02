package org.example.consumer;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.service.RoleAssignmentService;
import org.example.service.RoleManagementService;
import org.example.service.UserService;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ServerServiceConsumer {
    @Autowired
    RoleManagementService roleManagementService;

    @Autowired
    RoleAssignmentService roleAssignmentService;

    @Autowired
    UserService userService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @RabbitListener(queues = "SERVER-ROLE-QUEUE")
    public void receiveMessage(Message message){
        try {
            Map<String, String> messageBody = objectMapper.readValue(new String(message.getBody()), new TypeReference<Map<String, String>>() {});
            System.out.println(messageBody);
            if (userService.isMessageBrokerAuthorized(messageBody.get("Authorization")))
                switch (messageBody.get("action")){
                    case "delete-server" -> {
                        System.out.println("deleting server");
                        String serverId = messageBody.get("server-id");
                        if (serverId != null)
                            roleManagementService.deleteServerRoles(serverId);
                    }
                    case "create&assign-owner&member-role" -> {
                        System.out.println("assigning server owner");
                        String serverId = messageBody.get("server-id");
                        String serverOwner = messageBody.get("server-owner");
                        if (serverId != null && serverOwner != null) {
                            roleAssignmentService.createAndAssignOwnerRole(serverId, serverOwner);
                            roleAssignmentService.createMemberRole(serverId);
                        }
                    }

                    case "assign-member-role" -> {
                        System.out.println("assigning member role ");
                        String serverId = messageBody.get("server-id");
                        String username = messageBody.get("username");
                        if (serverId != null && username != null)
                            roleAssignmentService.assignMemberRole(serverId, username);
                    }
                }
        } catch (Exception ignored){ignored.printStackTrace();}


    }




}
