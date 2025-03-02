package org.example.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.RoomType;
import org.example.binary.tree.RoomConnectionsTree;
import org.example.config.RabbitConfig;
import org.example.model.Value.RoomConnection;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Map;

@Service
public class UserConnectionService { // the purpose of this service is to avoid having two connection of same time for same user.
    @Autowired
    StringRedisTemplate stringRedisTemplate;

    public void registerUserConnection(RoomConnection roomConnection, RoomConnectionService service) throws NoSuchAlgorithmException, InvalidKeySpecException, JsonProcessingException {
        String key = (roomConnection.getRoomType() == RoomType.VOCAL) ? "vocal-connection" : "textual-connection";
        String connectedToRoom = (String) stringRedisTemplate.opsForHash().get("user:" + roomConnection.getUsername(), key);
        if (connectedToRoom != null)
            service.sendMessageToDisconnectUserFromRoom(roomConnection.getUsername(), connectedToRoom);
        stringRedisTemplate.opsForHash().put("user:" + roomConnection.getUsername(), key, roomConnection.getRoomId());
    }

    public void registerUserDisconnection(RoomConnection roomConnection){
        String key = (roomConnection.getRoomType() == RoomType.VOCAL) ? "vocal-connection" : "textual-connection";
        String connectedToRoom = (String) stringRedisTemplate.opsForHash().get("user:" + roomConnection.getUsername(), key);
        if (connectedToRoom!= null && connectedToRoom.equals(roomConnection.getRoomId())){
            stringRedisTemplate.opsForHash().delete("user:" + roomConnection.getUsername(), key);
        }
    }






}
