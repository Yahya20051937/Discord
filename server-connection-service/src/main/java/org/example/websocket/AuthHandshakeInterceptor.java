package org.example.websocket;


import org.example.service.ServerConnectionService;
import org.example.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Component
public class AuthHandshakeInterceptor implements HandshakeInterceptor {

    @Autowired
    UserService userService;

    @Autowired
    ServerConnectionService serverConnectionService;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        try {
            String token = request.getHeaders()
                    .getFirst("Authorization");
            String username = userService.getUser(token).getUsername();

            String[] path = request.getURI().getPath().split("/");
            String serverId = path[path.length - 1];
            attributes.put("username", username);
            attributes.put("serverId", serverId);

                return true;
        } catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {

    }
}