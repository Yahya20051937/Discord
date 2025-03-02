package org.example.factory;

import org.example.Permission;
import org.example.entity.Role;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class RoleFactory {
    public static Role owner(String serverId){
        return Role.builder()
                .id(UUID.randomUUID().toString())
                .name("Owner")
                .serverId(serverId)
                .permissions(
                        Arrays.stream(Permission.values()).toList()
                )
                .ranking(1)
                .build();
    }

    public static Role member(String serverId){
        /* The id of this role will be the same as the server id, to make it easier to find it in the table when assigning it to a new member.
        * */
        return Role.builder()
                .id(serverId)
                .serverId(serverId)
                .name("member")
                .ranking(2)
                .permissions(new ArrayList<>())
                .roomsScopes(new ArrayList<>())
                .build();
    }
}
