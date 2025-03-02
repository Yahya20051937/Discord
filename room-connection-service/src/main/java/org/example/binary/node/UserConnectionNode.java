package org.example.binary.node;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.example.model.Value.UserConnection;

@Data
@EqualsAndHashCode(callSuper = true)
public class UserConnectionNode extends Node {
    private RoomConnectionNode roomConnectionNode;

    public UserConnectionNode(RoomConnectionNode roomConnectionNode){
        this.setValue(new UserConnection(roomConnectionNode.getNodeValue()));
        this.setRoomConnectionNode(roomConnectionNode);
    }



    public UserConnection getNodeValue(){
        return (UserConnection) this.getValue();
    }
}
