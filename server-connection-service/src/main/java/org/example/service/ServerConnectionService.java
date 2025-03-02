package org.example.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.ScopeType;
import org.example.binary.tree.ServerConnectionsTree;
import org.example.config.RabbitConfig;
import org.example.model.RoomDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ServerConnectionService {
    @Autowired
    ServerConnectionsTree serverConnectionsTree;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    WebClient webClient;

    @Autowired
    RabbitConfig rabbitConfig;

    public void shareMessage(String serverId,String actor , Map<String, String> notification) throws JsonProcessingException {
        Map<String , String> notificationBody = new HashMap<>();
        for (String key : notification.keySet())
            if (!key.equals("Authorization") && !key.equals("action") && !key.equals("serverId")) // remove unnecessary keys
                notificationBody.put(key, notification.get(key));
        serverConnectionsTree.sendNotificationToConnectedServerMembers(
                serverId,
                actor,
                objectMapper.writeValueAsString(notificationBody)
        );
    }



    public Boolean isUserMemberOfServer(String serverId, String username){
        return webClient.get()
                .uri("http://server-service/api/server/is-user-member-of-server?serverId=" + serverId + "&username=" + username)
                .retrieve()
                .bodyToMono(Boolean.class)
                .block();
    }
}
