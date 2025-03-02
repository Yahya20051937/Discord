package org.example.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.dto.request.CreateRoomPackageRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "room_package", indexes = {
        @Index(name = "idx_serverId_name", columnList = "serverId, name")
})
public class RoomPackage {   // a room must have a room package, when a server is created , we create two default packages (vocal rooms - text rooms)
    @Id
    private String id;
    private String name;
    private String serverId;

    @OneToMany(mappedBy = "roomPackage", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Room> rooms;

    public RoomPackage(CreateRoomPackageRequest createRoomPackageRequest){
        this.id = UUID.randomUUID().toString();
        this.name = createRoomPackageRequest.getName();
        this.serverId = createRoomPackageRequest.getServerId();
        this.rooms = new ArrayList<>();
    }
}
