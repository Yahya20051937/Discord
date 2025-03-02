package org.example.binary.node;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.example.model.Value.ServerConnection;

@Data
@EqualsAndHashCode(callSuper = true)
public class ServerConnectionNode extends Node{
    public ServerConnectionNode(ServerConnection serverConnection){
        super.setValue(
                serverConnection
        );
    }

    public ServerConnection getNodeValue(){
        return (ServerConnection) this.getValue();
    }
}
