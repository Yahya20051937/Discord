package org.example.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.ScopeType;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoomClientScopesDto {
    private String roomId;
    private String roomPackageId;
    private List<ScopeType> clientScopes;
}
