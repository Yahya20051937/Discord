package org.example.dto.response;

import lombok.Data;
import org.example.entitiy.Server;

@Data
public class ServerDto {
    private String id;
    private String name;

    public ServerDto(Server server){
        this.id = server.getId();
        this.name = server.getName();
    }
}
