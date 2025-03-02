package org.example.dto.response;

import lombok.Data;
import org.example.ScopeType;
import org.example.entity.RoomScope;

@Data
public class RoomScopeDto {
    private String id;
    private String roomId;
    private int roleRanking;
    private String roleId;
    private ScopeType scopeType;

    public RoomScopeDto(RoomScope roomScope){
        this.id = roomScope.getId();
        this.roomId = roomScope.getRoomId();
        this.roleRanking = roomScope.getRole().getRanking();
        this.scopeType = roomScope.getScopeType();
        this.roleId = roomScope.getRole().getId();
    }
}
