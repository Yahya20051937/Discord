package org.example.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.example.RoomAccessType;
import org.example.config.RabbitConfig;
import org.example.entity.Room;
import org.springframework.amqp.core.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Service
public class RoleService {

    @Autowired
    WebClient webClient;
    public boolean canUserCreateRoom(String token, String serverId){
        return Boolean.TRUE.equals(webClient.get()
                .uri("http://role-service/api/role/auth/permission/create-room?serverId=" + serverId)
                .header("Authorization", token)
                .retrieve()
                .bodyToMono(Boolean.class)
                .block());
    }


    public boolean canUserDeleteRoom(String token, Room room) {
        return Boolean.TRUE.equals(webClient.get()
            .uri("http://role-service/api/role/auth/permission/delete-room?serverId=" + room.getServerId() + "&roomId=" + room.getId())
            .header("Authorization", token)
            .retrieve()
            .bodyToMono(Boolean.class)
            .block()); }

    public boolean canUserDeleteRooms(String token, List<String> roomsIds, String serverId){
        return Boolean.TRUE.equals(
                    webClient.post()
                    .uri("http://role-service/api/role/auth/permission/delete-rooms?serverId=" + serverId)
                    .bodyValue(Map.of("roomsIds", roomsIds))
                    .header("Authorization", token)
                    .retrieve()
                    .bodyToMono(Boolean.class)
                    .block());
    }






}
