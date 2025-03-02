package org.example.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.RoomAccessType;
import org.example.RoomType;
import org.example.ScopeType;
import org.example.model.Value.RoomConnection;
import org.example.model.VocalMessage;
import org.example.service.RoomConnectionService;
import org.example.service.RoomScopeService;
import org.example.service.UserService;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

@Component
public class WebSocketHandler extends TextWebSocketHandler {
    @Autowired
    RoomScopeService roomScopeService;

    @Autowired
    RoomConnectionService roomConnectionService;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    UserService userService;


    @Override
    public void afterConnectionEstablished(@NotNull WebSocketSession webSocketSession) throws NoSuchAlgorithmException, InvalidKeySpecException, JsonProcessingException, InterruptedException {
        // user can only connect one vocal room and one text room at a time, so it's connected to the same room type that he wants to connect to, disconnect from the previous
        System.out.println("connection established");
        RoomConnection roomConnection = new RoomConnection(webSocketSession);
        String token = (String) webSocketSession.getAttributes().get("token");
        if ((roomConnection.getRoomAccessType() == RoomAccessType.PUBLIC && userService.isUserMemberOfServer(roomConnection.getServerId(), roomConnection.getUsername()))
                || (roomConnection.getRoomAccessType() == RoomAccessType.PRIVATE && roomScopeService.doesUserScope(
                roomConnection.getRoomId(),
                (roomConnection.getRoomType() == RoomType.VOCAL) ? ScopeType.JOIN : ScopeType.READ,
                token
                ))
        ) {

            roomConnectionService.handleConnection(roomConnection);
        }
    }

    @Override
    protected void handleTextMessage(@NotNull WebSocketSession socketSession, @NotNull TextMessage message){
        //System.out.println(message.getPayload());
        try {  // here we are doing vocal message that are received to this server, that will be sharing to connection here and to other servers.
            String payload = message.getPayload();
            VocalMessage vocalMessage = objectMapper.readValue(payload, new TypeReference<VocalMessage>() {});
            roomConnectionService.shareReceivedVocalMessage(vocalMessage);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void afterConnectionClosed(@NotNull WebSocketSession webSocketSession, @NotNull CloseStatus status) throws NoSuchAlgorithmException, InvalidKeySpecException {
        System.out.println("connection closed");
        String roomId = (String) webSocketSession.getAttributes().get("roomId");
        String username = (String) webSocketSession.getAttributes().get("username");
        roomConnectionService.handleDisconnection(roomId, username);
        //System.out.println(status.getReason());
        //System.out.println(status.getCode());
    }

    @Override
    public void handleTransportError(@NotNull WebSocketSession session, @NotNull Throwable exception) throws Exception {
        exception.printStackTrace();
    }




}
