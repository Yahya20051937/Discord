package org.example.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "roleUser", indexes = {
        @Index(name = "idx_username_role", columnList = "username, role_id"),
        @Index(name = "idx_role", columnList = "role_id"), // this will be used to find the role, and then on the role table there is an index for server id.
})
public class RoleUser {
    @Id
    private String id;
    private String username;



    @ManyToOne
    @JoinColumn(name = "role_id")
    private Role role;

    public RoleUser(String username, Role role){
        this.id = UUID.randomUUID().toString();
        this.username = username;
        this.role = role;
    }
}
