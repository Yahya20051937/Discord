package org.example.websocket;

import com.netflix.discovery.converters.Auto;
import org.example.RoomType;
import org.example.dto.RoomInfo;
import org.example.service.RoomConnectionService;
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
    RoomConnectionService roomConnectionService;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        try {
            String token = request.getHeaders()
                    .getFirst("Authorization");
            String username = userService.getUser(token).getUsername() ;

            String[] path = request.getURI().getPath().split("/");
            String roomId = path[path.length - 1];

            RoomInfo roomInfo = roomConnectionService.getRoomInfo(roomId);
            if (roomInfo != null) {
                attributes.put("username", username);
                attributes.put("roomId", roomId);
                attributes.put("token", token);
                attributes.put("roomAccessType", roomInfo.getRoomAccessType());
                attributes.put("roomType", roomInfo.getRoomType());
                attributes.put("serverId", roomInfo.getServerId());
                attributes.put("roomPackageId", roomInfo.getRoomPackageId());

                return true;
            }
            return false;
        } catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {

    }
}