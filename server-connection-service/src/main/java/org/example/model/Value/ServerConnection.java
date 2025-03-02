package org.example.model.Value;

import lombok.Data;
import org.example.service.RoomScopeService;
import org.example.service.ServerConnectionService;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Map;

@Data
public class ServerConnection implements Value{
    private String serverId;
    private String username;
    private WebSocketSession webSocketSession;

    @Override
    public String getAttribute(String key) {
        return switch (key){
            case "serverId" -> this.serverId;
            case "username" -> this.username;
            default -> null;
        };

    }

    private void shareNotification(String json){
        try {
            this.webSocketSession.sendMessage(new TextMessage(json));
        } catch (IOException e){
            e.printStackTrace();
        }

    }

    public ServerConnection(WebSocketSession webSocketSession){
        this.serverId = (String) webSocketSession.getAttributes().get("serverId");
        this.username = (String) webSocketSession.getAttributes().get("username");
        this.webSocketSession = webSocketSession;
    }

    @Override
    public void performAction(String action, Object... args){
        if (action.equals("share-notification")){
            try {
                String notificationJson = (String) args[0];
                String actor = (String) args[1];
                if (!actor.equals(this.username))
                    this.shareNotification(notificationJson);
            } catch (Exception e){
                e.printStackTrace();;
            }

        }

    }
}
