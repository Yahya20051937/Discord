package org.example.service;

import org.example.ScopeType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class RoomScopeService {
    @Autowired
    WebClient webClient;

    public Boolean doesUserScope(String roomId, ScopeType scopeType, String token){
        return webClient.get()
                .uri("http://role-service/api/role/auth/permission/scope-in-room/" + scopeType + "?roomId=" + roomId)
                .header("Authorization", token)
                .retrieve()
                .bodyToMono(Boolean.class)
                .block();
    }
}
