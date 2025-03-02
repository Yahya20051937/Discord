package org.example.repository;

import org.example.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, String> {
    @Query("SELECT m FROM Message m WHERE :a < m.counter AND m.counter <= :b AND m.roomId = :roomId ORDER BY m.counter ASC")
    List<Message> findLastNMessagesBeforeX(@Param("a") long a, @Param("b") long b, @Param("roomId") String roomId);

    void deleteByRoomId(String roomId);

    long countByRoomId(String roomId);
}
