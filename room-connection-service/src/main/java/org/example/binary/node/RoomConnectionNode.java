package org.example.binary.node;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.example.model.Value.RoomConnection;
import org.intellij.lang.annotations.JdkConstants;

import java.util.UUID;

@Data

@EqualsAndHashCode(callSuper = true)
public class RoomConnectionNode extends Node {
    public RoomConnectionNode(RoomConnection value){
        this.setValue(value);
    }

    public RoomConnectionNode(){
        this.setValue(RoomConnection.builder()
                .id(UUID.randomUUID().toString())
                .roomId(UUID.randomUUID().toString())
                .build());
    }

    public RoomConnectionNode(String roomId){
        this.setValue(RoomConnection.builder()
                .id(UUID.randomUUID().toString())
                .roomId(roomId)
                .build());
    }

    public RoomConnectionNode(String roomId, String username){
        this.setValue(RoomConnection.builder()
                .id(UUID.randomUUID().toString())
                .roomId(roomId)
                .username(username)
                .build());
    }

    @JsonIgnore
    public RoomConnectionNode getLeftNode(){
        return (RoomConnectionNode) this.getLeft();
    }

    @JsonIgnore
    public RoomConnectionNode getRightNode(){
        return (RoomConnectionNode) this.getRight();
    }

    @JsonIgnore
    public RoomConnection getNodeValue(){
        return (RoomConnection) this.getValue();
    }





}
