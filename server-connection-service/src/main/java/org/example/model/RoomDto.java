package org.example.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.RoomType;
import org.example.ScopeType;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoomDto{
    private String id;
    private String name;
    private String serverId;
    private RoomType roomType;

    private List<ScopeType> clientScopes;


}
