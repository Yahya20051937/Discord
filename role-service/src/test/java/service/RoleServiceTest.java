package service;

import org.example.Permission;
import org.example.entity.Role;
import org.example.repository.RoleRepository;
import org.example.repository.RoleUserRepository;
import org.example.service.RoleManagementService;
import org.example.service.RoleUserService;
import org.example.service.ServerService;
import org.example.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
public class RoleServiceTest {
    @Mock
    RoleRepository roleRepository;

    @Mock
    UserService userService;

    @Mock
    RoleUserService roleUserService;

    @Mock
    ServerService serverService;

    @InjectMocks
    RoleManagementService managementService;

    private List<Role> mockServerRoles;
    private String mockServerId;


    @BeforeEach
    void setUp(){
        this.mockServerId = UUID.randomUUID().toString();
        this.mockServerRoles = List.of(
                Role.builder()
                        .id(UUID.randomUUID().toString())
                        .name("admin")
                        .ranking(1)
                        .serverId(this.mockServerId)
                        .permissions(List.of(Permission.ASSIGN_ROLE, Permission.CREATE_ROLES))
                        .build(),
                Role.builder()
                        .id(UUID.randomUUID().toString())
                        .name("admin")
                        .ranking(2)
                        .serverId(this.mockServerId)
                        .permissions(List.of(Permission.ASSIGN_ROLE, Permission.CREATE_ROLES))
                        .build(),
                Role.builder()
                        .id(UUID.randomUUID().toString())
                        .name("admin")
                        .ranking(3)
                        .serverId(this.mockServerId)
                        .permissions(List.of(Permission.ASSIGN_ROLE, Permission.CREATE_ROLES))
                        .build(),
                Role.builder()
                        .id(UUID.randomUUID().toString())
                        .name("admin")
                        .ranking(4)
                        .serverId(this.mockServerId)
                        .permissions(List.of(Permission.ASSIGN_ROLE, Permission.CREATE_ROLES))
                        .build()

        );
    }

    @Test
    void testIncrementPostOrderRolesOrders(){
        /*this.mockServerRoles.add(
                Role.builder()
                        .id(UUID.randomUUID().toString())
                        .name("admin")
                        .order(2)
                        .server_id(this.mockServerId)
                        .permissions(List.of(Permission.ASSIGN_ROLE, Permission.CREATE_ROLES))
                        .build()
        );*/

        managementService.incrementPostOrderRolesOrders(this.mockServerRoles, 2);

        assertEquals(1, this.mockServerRoles.get(0).getRanking());
        assertEquals(3, this.mockServerRoles.get(1).getRanking());
        assertEquals(4, this.mockServerRoles.get(2).getRanking());
        assertEquals(5, this.mockServerRoles.get(3).getRanking());
    }
}
