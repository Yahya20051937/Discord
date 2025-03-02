package org.example.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.RoomType;
import org.example.binary.node.Node;
import org.example.binary.node.RoomConnectionNode;
import org.example.binary.node.UserConnectionNode;
import org.example.binary.tree.RoomConnectionsTree;
import org.example.config.RabbitConfig;
import org.example.config.WebClientConfig;
import org.example.dto.RoomDto;
import org.example.dto.RoomInfo;
import org.example.model.MessageInt;
import org.example.model.Value.RoomConnection;
import org.example.model.VocalMessage;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public class RoomConnectionService {
    @Autowired
    RoomConnectionsTree roomConnectionsTree;

    @Autowired
    WebClient webClient;

    @Autowired
    WebClientConfig webClientConfig;

    @Autowired
    RabbitConfig rabbitConfig;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    public UserConnectionService userConnectionService;

    @Autowired
    ServerNotificationService serverNotificationService;

    public void handleConnection(RoomConnection roomConnection) throws NoSuchAlgorithmException, InvalidKeySpecException, JsonProcessingException, InterruptedException {
        System.out.println("Connecting to room " + roomConnection.getRoomId() + " " + roomConnection.getRoomType() + " " + roomConnection.getUsername());
        roomConnectionsTree.insert(new RoomConnectionNode(roomConnection));
        userConnectionService.registerUserConnection(roomConnection, this);
        if (roomConnection.getRoomType() == RoomType.VOCAL) // we only need it when the room is vocal.
            serverNotificationService.notifyUserConnection(roomConnection);
    }

    public void handleDisconnection(String roomId, String username) throws NoSuchAlgorithmException, InvalidKeySpecException {
        System.out.println("disconnectFromRoom " + roomId);
        RoomConnectionNode roomConnectionNode = roomConnectionsTree.findByRoomIdAndUsername(roomId, username);

        if (roomConnectionNode != null) {
            this.handleDisconnection(roomConnectionNode.getNodeValue()); // we should do this first, because if we inverse , the value of the node changes because of deletion.
            roomConnectionsTree.deleteNode(roomConnectionNode);
        }

    }

    public void handleDisconnection(RoomConnection roomConnection) throws NoSuchAlgorithmException, InvalidKeySpecException {
        if (roomConnection.getRoomType() == RoomType.VOCAL)
            serverNotificationService.notifyUserDisconnection(roomConnection);
        userConnectionService.registerUserDisconnection(roomConnection);
    }

    public void shareMessage(MessageInt message) throws IOException {
        roomConnectionsTree.sendMessageToConnectedRoomMembers(message.getRoomId(), message);
    }

    public void shareReceivedVocalMessage(VocalMessage message) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        /*
        * Here we share the message we received to connection in this server, and we put in  all servers queues to get sent to connections in other servers.
        * */
        CompletableFuture.runAsync(
                () -> {
                    try {
                        this.shareMessage(message);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
        );
        Map<String, String> queueMessageBody = Map.of(
                "action", "share-vocal-message",
                "Authorization", rabbitConfig.messagingToken(),
                "message", objectMapper.writeValueAsString(message)
        );
        for (String queueName : rabbitConfig.getAllRoomConnectionsQueues()) {
            String id = queueName.split("/")[2];
            if (!id.equals(rabbitConfig.queueId)) {
                String exchange = "room-connections/exchange/" + id;
                String routingKey = "room-connections/routing-key/" + id;
                rabbitTemplate.convertAndSend(
                        exchange,
                        routingKey,
                        new org.springframework.amqp.core.Message(objectMapper.writeValueAsString(queueMessageBody).getBytes(StandardCharsets.UTF_8)),
                        m -> {
                            m.getMessageProperties().setContentType("application/json");
                            return m;
                        }
                );

            }
        }
    }

    public void sendMessageToDisconnectUserFromRoom(String username, String roomId) throws NoSuchAlgorithmException, InvalidKeySpecException, JsonProcessingException {
        // disconnect in the server first then send the message to other servers.
        System.out.println("Disconnecting " + username + "from room " + roomId +  " automatically");
        this.disconnectUserManuallyFromRoom(roomId, username);
        Map<String, String> queueMessageBody = Map.of(
                "action", "disconnect-user-from-room",
                "Authorization", rabbitConfig.messagingToken(),
                "username", username,
                "roomId", roomId
        );
        for (String queueName : rabbitConfig.getAllRoomConnectionsQueues()) {
            String id = queueName.split("/")[2];
            if (!id.equals(rabbitConfig.queueId)) {
                String exchange = "room-connections/exchange/" + id;
                String routingKey = "room-connections/routing-key/" + id;
                rabbitTemplate.convertAndSend(
                        exchange,
                        routingKey,
                        new org.springframework.amqp.core.Message(objectMapper.writeValueAsString(queueMessageBody).getBytes(StandardCharsets.UTF_8)),
                        m -> {
                            m.getMessageProperties().setContentType("application/json");
                            return m;
                        }
                );

            }
        }
    }

    public RoomInfo getRoomInfo(String roomId){
        return webClient.get()
                .uri("http://room-service/api/room/getInfo?roomId=" + roomId)
                .retrieve()
                .bodyToMono(RoomInfo.class)
                .block();
    }

    public void disconnectFromRoomAndDelete(String roomId){
        roomConnectionsTree.deleteAndDisconnectByRoomId(roomId);
    }

    public void disconnectUserManuallyFromRoom(String roomId, String username){
        System.out.println("Disconnecting Manually Process Started");
        roomConnectionsTree.disconnectByRoomIdAndUsername(roomId, username);
    }

    public void fillRoomsConnectedMembers(List<RoomDto> rooms){ // this will be called from room-service, and to all servers as well.
        for (RoomDto room : rooms)
            if (room.getRoomType() == RoomType.VOCAL) // we need connected members only for vocal rooms.
                roomConnectionsTree.findAllByRoomId(room.getId())
                    .forEach(
                            it -> room.addMember(((RoomConnection) it.getValue()).getUsername())
                    );

    }

    public void handleDisconnectionToAll(){
        roomConnectionsTree.performToAll("handle-disconnect", this);
    }
}
