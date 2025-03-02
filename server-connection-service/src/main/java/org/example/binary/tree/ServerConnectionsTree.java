package org.example.binary.tree;

import org.example.service.RoomScopeService;
import org.example.service.ServerConnectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ServerConnectionsTree extends Tree {
    {
        this.setMainAttribute("serverId");
    }

    public void sendNotificationToConnectedServerMembers(String serverId, String actor , String notificationJson){
        super.findAllAndPerform(serverId, super.getHead(), "share-notification", notificationJson, actor);
    }

    public void deleteByServerIdAndUsername(String serverId, String username){
        super.delete(serverId, Map.of("username", username));
    }
}
