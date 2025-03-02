package org.example.repository;

import org.example.entitiy.MemberShip;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MemberShipRepository extends JpaRepository<MemberShip, String> {
    Optional<MemberShip> findByMemberAndServerId(String member, String serverId);
    List<MemberShip> findByMember(String member); // both are using the same index.

    List<MemberShip> findByServerId(String serverId);
}
