package org.example.model.Value;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.RoomAccessType;
import org.example.RoomType;
import org.example.model.MessageInt;
import org.example.service.RoomConnectionService;
import org.example.service.ServerNotificationService;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.LinkedList;

@Data
@AllArgsConstructor
@NoArgsConstructor(force = true)
@Builder
public class RoomConnection implements Value {
    private final String id;
    private String roomId;
    private final String username;
    private String roomPackageId;
    private String serverId;
    @JsonIgnore
    private WebSocketSession webSocketSession;
    private RoomType roomType;
    private RoomAccessType roomAccessType;

    @JsonIgnore
    private LinkedList<MessageInt> messagesQueue = new LinkedList<>();

    public RoomConnection(WebSocketSession webSocketSession){
        this.webSocketSession = webSocketSession;
        this.id = webSocketSession.getId();
        this.username = (String) webSocketSession.getAttributes().get("username");
        this.roomId = (String) webSocketSession.getAttributes().get("roomId");
        this.roomPackageId = (String) webSocketSession.getAttributes().get("roomPackageId");
        this.serverId = (String) webSocketSession.getAttributes().get("serverId");
        this.roomType = (RoomType) webSocketSession.getAttributes().get("roomType");
    }

    public void sendMessage(String message) throws IOException {
        if (this.webSocketSession.isOpen()) {
            this.webSocketSession.sendMessage(new TextMessage(message));
        }
    }

    @Override
    public String getAttribute(String key) {
        return switch (key) {
            case "id" -> this.id;
            case "roomId" -> this.getRoomId();
            case "username" -> this.getUsername();
            case "serverId" -> this.getServerId();
            case "roomType" -> this.getRoomType().name();
            default -> null;
        };
    }

    @Override
    public void performAction(String action, Object...args){
        try {
            if (action.equals("send-message")) {
                MessageInt message = (MessageInt) args[0];
                if (!message.getSender().equals(this.username)) // to avoid sending to the message writer.
                    this.sendMessage(message.toJson());
            } else if (action.equals("disconnect"))
                this.webSocketSession.close();
            else if (action.equals("handle-disconnect")){
                RoomConnectionService roomConnectionService = (RoomConnectionService) args[0];
                roomConnectionService.handleDisconnection(this);
            }

        } catch (Exception e){
            e.printStackTrace();
        }

    }

    private void sendMessages(){
        while (this.webSocketSession.isOpen()){


        }

    }


}
