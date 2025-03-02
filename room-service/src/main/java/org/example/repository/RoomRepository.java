package org.example.repository;

import org.example.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RoomRepository extends JpaRepository<Room, String> {
    public Optional<Room> findByRoomPackage_ServerIdAndName(String serverId, String name);
    public List<Room> findByRoomPackage_ServerId(String serverId);
    List<Room> findByRoomPackageId(String roomPackageId);
}
