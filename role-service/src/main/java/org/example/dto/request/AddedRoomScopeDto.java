package org.example.dto.request;

import lombok.Data;
import org.example.ScopeType;

@Data
public class AddedRoomScopeDto {
    private String roomId;
    private ScopeType scopeType;
}
