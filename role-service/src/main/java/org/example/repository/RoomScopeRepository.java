package org.example.repository;

import org.example.entity.RoomScope;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface RoomScopeRepository extends JpaRepository<RoomScope, String> {
    @Query("SELECT r FROM RoomScope r WHERE roomId = ?1 ORDER BY r.role.ranking ASC")
    List<RoomScope> findByRoomIdSortedByOrder(String roomId);

    List<RoomScope> findByRoomId(String roomId);

    List<RoomScope> findByRole_ServerId(String serverId);

    List<RoomScope> findByRoomIdAndRoleId(String roomId,String roleId);
}
