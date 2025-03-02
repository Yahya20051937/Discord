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

    private User getUser(HttpServletRequest request){
        String token = request.getHeader("Authorization");
        return this.sendRequest(token);
    }

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

    public Boolean isUserMemberOfServer(String serverId, String username){
        return webClient.get()
                .uri("http://server-service/api/server/is-user-member-of-server?serverId="  + serverId + "&username=" + username)
                .retrieve()
                .bodyToMono(Boolean.class)
                .block();
    }
}