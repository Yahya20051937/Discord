package org.example.entitiy;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.dto.request.CreateRequest;

import java.util.List;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "server", indexes = {
        @Index(name = "idx_name", columnList = "name")
})
public class Server {
    @Id
    private String id;
    private String name;
    private String owner;

    @OneToMany(mappedBy = "server", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MemberShip> memberShips;

    public Server(CreateRequest createRequest, String username){
        this.id = UUID.randomUUID().toString();
        this.name = createRequest.getName();
        this.owner = username;
    }
}
