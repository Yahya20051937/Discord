package org.example.repository;

import org.example.entitiy.Server;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ServerRepository extends JpaRepository<Server, String> {
    List<Server> findByNameContaining(String substring);
}
