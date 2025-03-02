package org.example.service;

import jakarta.servlet.http.HttpServletRequest;
import org.example.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class UserService {
    @Autowired
    WebClient webClient;

    public User getUser(String token){
        return this.sendRequest(token);
    }

    private User sendRequest(String token){
        return webClient.get()
                .uri("http://user-service/api/user/get")
                .header("Authorization", token)
                .retrieve()
                .bodyToMono(User.class)
                .block();
    }

    public Boolean isMessageBrokerAuthorized(String token){
        return webClient.get()
                .uri("http://user-service/api/user/message-broker/authorized")
                .header("token", token)
                .retrieve()
                .bodyToMono(Boolean.class)
                .block();
    }
}