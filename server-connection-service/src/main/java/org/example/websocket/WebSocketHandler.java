package org.example.websocket;

import org.example.ScopeType;

import org.example.binary.node.ServerConnectionNode;
import org.example.binary.tree.ServerConnectionsTree;
import org.example.model.Value.ServerConnection;
import org.example.service.ServerConnectionService;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

@Component
public class WebSocketHandler extends TextWebSocketHandler {


    @Autowired
    ServerConnectionService serverConnectionService;

    @Autowired
    ServerConnectionsTree serverConnectionsTree;


    @Override
    public void afterConnectionEstablished(@NotNull WebSocketSession webSocketSession) throws NoSuchAlgorithmException, InvalidKeySpecException {
        // user can only connect one vocal room and one text room at a time, so it's connected to the same room type that he wants to connect to, disconnect from the previous
         ServerConnection serverConnection = new ServerConnection(webSocketSession);
        if (serverConnectionService.isUserMemberOfServer(serverConnection.getServerId(), serverConnection.getUsername()))
            serverConnectionsTree.insert(new ServerConnectionNode(serverConnection));

    }

    @Override
    public void afterConnectionClosed(@NotNull WebSocketSession webSocketSession, @NotNull CloseStatus status) throws NoSuchAlgorithmException, InvalidKeySpecException {
        String serverId = (String) webSocketSession.getAttributes().get("serverId");
        String username = (String) webSocketSession.getAttributes().get("username");
        serverConnectionsTree.deleteByServerIdAndUsername(serverId, username);

    }
}
