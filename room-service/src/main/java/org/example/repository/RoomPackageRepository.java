package org.example.repository;

import org.example.entity.RoomPackage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RoomPackageRepository extends JpaRepository<RoomPackage, String> {
    Optional<RoomPackage> findByServerIdAndName(String serverId, String name);

    void deleteByServerId(String serverId);
    List<RoomPackage> findByServerId(String serverId);
}
