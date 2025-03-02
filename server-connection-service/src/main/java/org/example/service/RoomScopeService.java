package org.example.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.ScopeType;
import org.example.config.RabbitConfig;
import org.example.model.RoomDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.socket.WebSocketSession;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.List;
import java.util.Map;

@Service
public class RoomScopeService {
    @Autowired
    WebClient webClient;
    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    RabbitConfig rabbitConfig;

    // the room is just created, it doesn't have any scopes.

    /*public String fillRoomClientScopesIfRoomWasCreated(String notificationJson, String username){ // not tested.
        try {
            Map<String, Object> notificationMap = objectMapper.convertValue(notificationJson, new TypeReference<Map<String, Object>>() {});
            if (notificationMap.get("type").equals("room_created")){
                RoomDto room = objectMapper.convertValue(notificationMap.get("room"), new TypeReference<RoomDto>() {});
                this.fillRoomClientScopes(room, username);
                notificationMap.put("room", objectMapper.writeValueAsString(room));
            }
            return objectMapper.writeValueAsString(notificationMap);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException | JsonProcessingException e){
            e.printStackTrace();
        }
        return notificationJson;


    }*/

    /*private void fillRoomClientScopes(RoomDto room, String username) throws NoSuchAlgorithmException, InvalidKeySpecException {
        List<ScopeType> roomClientScopes = webClient
                .get()
                .uri("http://role-service/api/role/internal/get/room-client-scopes?roomId=" + room +"&username=" + username)
                .header("token", rabbitConfig.messagingToken())
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<ScopeType>>() {})
                .block();
        room.setClientScopes(roomClientScopes);
    }*/
}
