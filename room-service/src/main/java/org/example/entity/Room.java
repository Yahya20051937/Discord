package org.example.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.RoomAccessType;
import org.example.RoomType;
import org.example.dto.request.CreateRoomRequest;

import java.util.UUID;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "room", indexes = {
        @Index(name = "idx_roomPackage", columnList = "roomPackage_id"),
        @Index(name = "idx_name", columnList = "name")
})
public class Room {
    @Id
    private String id;
    private String name;
    private RoomType roomType;

    private RoomAccessType roomAccessType;

    @ManyToOne
    @JoinColumn(name = "room_package_id")
    private RoomPackage roomPackage;

    public Room(CreateRoomRequest request, RoomPackage roomPackage){
        this.id = UUID.randomUUID().toString();
        this.name = request.getName();
        this.roomType = request.getRoomType();
        this.roomPackage  = roomPackage;
        this.roomAccessType = request.getRoomAccessType();
    }

    public String getServerId(){
        return this.roomPackage.getServerId();
    }
}
