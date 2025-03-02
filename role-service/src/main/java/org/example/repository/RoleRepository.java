package org.example.repository;

import org.example.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, String> {
    Optional<Role> findByServerIdAndName(String serverId, String name); // index server_id , name
    List<Role> findByServerId(String serverId); // index server_id, order

}
