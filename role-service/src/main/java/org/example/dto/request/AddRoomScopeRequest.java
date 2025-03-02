package org.example.dto.request;

import lombok.Data;
import org.example.ScopeType;

@Data
public class AddRoomScopeRequest {
    private String roleId;
    private String roomId;
    private ScopeType scopeType;
}
