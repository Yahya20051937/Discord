package org.example.service;

import org.example.RoomAccessType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class ServerService {
    @Autowired
    WebClient webClient;

    public boolean isUserMemberOfThisServer(String serverId, String username){  // tested

        return Boolean.TRUE.equals(webClient.get()
                .uri("http://server-service/api/server/is-user-member-of-server?serverId=" + serverId + "&username=" + username)
                .retrieve()
                .bodyToMono(Boolean.class)
                .block()
        );
    }
   /* public boolean doesRoomBelongToServer(String serverId, String roomId) {
        return Boolean.TRUE.equals(
                webClient.get()
                        .uri("http://room-service/api/room/belongs-to-server?roomId=" + roomId + "&serverId=" + serverId)
                        .retrieve()
                        .bodyToMono(Boolean.class)
                        .block()
                );

    }*/
}
