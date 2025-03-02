package org.example.repository;

import org.example.entity.Role;
import org.example.entity.RoleUser;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface RoleUserRepository extends JpaRepository<RoleUser, String> {
    @Query("Select r from RoleUser r where r.username = ?1 and r.role.serverId = ?2 ORDER BY r.role.ranking ASC")
    List<RoleUser> findByUsernameAndServerIdSorted(String username, String serverId); // username index in role_user table and role.server.id index in role table

    List<RoleUser> findByUsernameAndRole_ServerId(String username, String serverId); // ""

    Optional<RoleUser> findByUsernameAndRoleId(String username, String roleId);

    List<RoleUser> findByRole_ServerId(String serverId);

    List<RoleUser> findByUsername(String username);
    List<RoleUser> findByRoleId(String roleId);

}
