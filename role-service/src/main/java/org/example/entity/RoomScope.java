package org.example.entity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.ScopeType;
import org.example.dto.request.AddedRoomScopeDto;

import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "roomScope", indexes = {
        @Index(name = "roomId_roleId_idx", columnList = "roomId, role_id"),  // here it would be better to start with role when searching, but i then would have to create another index for roomId, but with this index , i can search for roomId
        @Index(name = "roleId_idx", columnList = "role_id")
})
public class RoomScope { // this is entity that tells us that a user with this role can access this room, and do the allowed action
    @Id
    private String id;
    private String roomId;

    @ManyToOne
    @JoinColumn(name = "role_id")
    private Role role;

    private ScopeType scopeType;

    public RoomScope(AddedRoomScopeDto dto, Role role){
        this.id = UUID.randomUUID().toString();
        this.roomId = dto.getRoomId();
        this.scopeType = dto.getScopeType();
        this.role = role;
    }

}
