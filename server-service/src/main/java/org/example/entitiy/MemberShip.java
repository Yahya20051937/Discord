package org.example.entitiy;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "memberShip", indexes = {
        @Index(name = "idx_member_serverId", columnList = "member, serverId"),
        @Index(name = "idx_serverId", columnList = "serverId")
})
public class MemberShip {
    @Id
    private String id;
    private String member;

    @ManyToOne
    @JoinColumn(name = "server_id")
    private Server server;

    public MemberShip(String member, Server server){
        this.id = UUID.randomUUID().toString();
        this.member = member;
        this.server = server;
    }
}
