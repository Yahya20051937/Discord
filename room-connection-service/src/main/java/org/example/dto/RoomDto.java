package org.example.dto;

import lombok.Data;
import org.example.RoomAccessType;
import org.example.RoomType;
import org.example.ScopeType;

import java.util.ArrayList;
import java.util.List;

@Data
public class RoomDto {
    private String id;
    private String name;
    private String serverId;
    private RoomType roomType;
    private RoomAccessType roomAccessType;
    private String roomPackageId;
    private List<ScopeType> clientScopes;
    private List<String> connectedMembers;
    private Integer mainRoleRanking;
    private  String type;

    public void addMember(String member){
        if (connectedMembers == null)
            connectedMembers = new ArrayList<>();
        connectedMembers.add(member);
    }
}
