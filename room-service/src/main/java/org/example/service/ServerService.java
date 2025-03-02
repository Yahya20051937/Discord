package org.example.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class ServerService {
    @Autowired
    WebClient webClient;

    public Boolean isServerPresent(String serverId){
        return webClient.get()
                .uri("http://server-service/api/server/is-present?id=" + serverId)
                .retrieve()
                .bodyToMono(Boolean.class)
                .block();
    }
}
