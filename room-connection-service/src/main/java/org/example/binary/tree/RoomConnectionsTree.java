package org.example.binary.tree;

import org.example.RoomType;
import org.example.binary.node.Node;
import org.example.binary.node.RoomConnectionNode;
import org.example.model.Message;
import org.example.model.MessageInt;
import org.example.service.RoomConnectionService;
import org.example.service.ServerNotificationService;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Map;


@Component
public class RoomConnectionsTree extends Tree {
    {
        this.setMainAttribute("roomId");
    }


    public void sendMessageToConnectedRoomMembers(String roomId, MessageInt message){
        super.findAllAndPerform(roomId, super.getHead(), "send-message", message);
    }

    public RoomConnectionNode findRoomConnectionParent(RoomConnectionNode node){
        return (RoomConnectionNode) this.findParent(node);
    }

    /*public RoomConnectionNode findByRoomIdAndId(String roomId, String id){
        return (RoomConnectionNode) this.findByAttributes(roomId, Map.of("id", id));
    }*/

    public RoomConnectionNode findByRoomIdAndUsername(String roomId, String username){
        return (RoomConnectionNode) this.findByAttributes(roomId, Map.of("username", username));
    }

    public List<RoomConnectionNode> findAllByRoomId(String roomId){
        return super.findAll(roomId)
                .stream()
                .map(it -> (RoomConnectionNode) it)
                .toList();
    }

    public void disconnectByRoomIdAndUsername(String roomId, String username){
        Node node = this.findByAttributes(roomId, Map.of("username", username));
        if (node != null)
            node.getValue().performAction("disconnect");
    }

    public void delete(String roomId, String id) {
        super.delete(roomId , Map.of("id", id));
    }

    public void deleteAndDisconnectByRoomId(String roomId){
        super.deleteAndPerform(roomId,Map.of(), super.getHead() ,"disconnect");
    }


}






























