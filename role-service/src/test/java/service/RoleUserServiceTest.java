package service;


import org.example.Permission;
import org.example.entity.Role;
import org.example.entity.RoleUser;
import org.example.repository.RoleUserRepository;
import org.example.repository.RoomScopeRepository;
import org.example.service.RoleUserService;
import org.example.service.RoomScopeService;
import org.example.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class RoleUserServiceTest {
    @Mock
    RoleUserRepository roleUserRepository;
    @Mock
    RoomScopeService roomScopeService;
    @Mock
    UserService userService;

    @Mock
    RoomScopeRepository roomScopeRepository;

    @InjectMocks
    RoleUserService service;

    private RoleUser mockRoleUser1;
    private RoleUser mockRoleUser2;
    private RoleUser mockRoleUser3;
    private RoleUser mockRoleUser4;

    private String serverId;

    private Role mockRole1;
    private Role mockRole2;
    private Role mockRole4;

    @BeforeEach
    void setUp(){
        MockitoAnnotations.openMocks(this);
        this.serverId = UUID.randomUUID().toString();
        this.mockRole1 = Role.builder()
                .id(UUID.randomUUID().toString())
                .name("admin")
                .ranking(3)
                .serverId(this.serverId)
                .permissions(List.of(Permission.ASSIGN_ROLE, Permission.CREATE_ROLES))
                .build();
        this.mockRole2 = Role.builder()
                .id(UUID.randomUUID().toString())
                .name("admin")
                .ranking(1)
                .serverId(this.serverId)
                .permissions(List.of(Permission.CREATE_ROOM, Permission.DELETE_ROOM))
                .build();
        this.mockRole4 = Role.builder()
                .id(UUID.randomUUID().toString())
                .name("admin")
                .ranking(1)
                .serverId("fjwjfjwfwpkfwpkfp[wk")
                .permissions(List.of(Permission.CREATE_ROOM, Permission.DELETE_ROOM))
                .roomsScopes(List.of())
                .build();

        this.mockRoleUser1 = RoleUser.builder()
                .id(UUID.randomUUID().toString())
                .role(this.mockRole1)
                .username("yahya")
                .build();
        this.mockRoleUser2 = RoleUser.builder()
                .id(UUID.randomUUID().toString())
                .role(this.mockRole2)
                .username("anouar")
                .build();

        this.mockRoleUser3 = RoleUser.builder()
                .id(UUID.randomUUID().toString())
                .role(this.mockRole1)
                .username("anouar")
                .build();

        this.mockRoleUser4 = RoleUser.builder()
                .id(UUID.randomUUID().toString())
                .role(this.mockRole4)
                .username("yahya")
                .build();
    }

    @Test
    void doesUserHavePermissionInServiceTest(){
        when(roleUserRepository.findByUsernameAndRole_ServerId("yahya", this.serverId))
                .thenReturn(List.of(this.mockRoleUser1));

        assertTrue(
                service.doesUserHavePermissionInServer(Permission.ASSIGN_ROLE, this.serverId, "yahya")
        );
        assertFalse(
                service.doesUserHavePermissionInServer(Permission.DELETE_ROOM, this.serverId, "yahya")
        );

        when(roleUserRepository.findByUsernameAndRole_ServerId("anouar", this.serverId))
                .thenReturn(List.of(this.mockRoleUser2));

        assertTrue(
                service.doesUserHavePermissionInServer(Permission.CREATE_ROOM, this.serverId, "anouar")
        );
        assertFalse(
                service.doesUserHavePermissionInServer(Permission.ASSIGN_ROLE, this.serverId, "anouar")
        );
    }

    @Test
    void canUserAssignRole(){
        when(roleUserRepository.findByUsernameAndRole_ServerId("yahya", this.serverId))
                .thenReturn(List.of(this.mockRoleUser1));
        when(roleUserRepository.findByUsernameAndServerIdSorted("yahya", this.serverId))
                .thenReturn(List.of(this.mockRoleUser1));

        assertFalse(
                service.canUserAssignRole("yahya", this.mockRole2)
        );
        assertTrue(
                service.canUserAssignRole("yahya", this.mockRole1)
        );


        when(roleUserRepository.findByUsernameAndRole_ServerId("anouar", this.serverId))
                .thenReturn(List.of(this.mockRoleUser3, this.mockRoleUser2));
        when(roleUserRepository.findByUsernameAndServerIdSorted("anouar", this.serverId))
                .thenReturn(List.of(this.mockRoleUser2, this.mockRoleUser3));

        assertTrue(
                service.canUserAssignRole("anouar", this.mockRole1)
        );
        assertTrue(
                service.canUserAssignRole("anouar", this.mockRole2)
        );


        when(roleUserRepository.findByUsernameAndRole_ServerId("anouar", this.serverId))
                .thenReturn(List.of(this.mockRoleUser2));

        assertFalse(
                service.canUserAssignRole("anouar", this.mockRole2)
        );

        assertFalse(
                service.canUserAssignRole("anouar", this.mockRole1)
        );

    }

    @Test
    void testGetClientServerPermissions(){
        when(roleUserRepository.findByUsername("yahya")).thenReturn(List.of(this.mockRoleUser1, this.mockRoleUser4));
        assertEquals(
                service.getClientServerPermissionsByUsername(this.serverId, "yahya"),
                List.of(Permission.ASSIGN_ROLE, Permission.CREATE_ROLES)
        );

        assertEquals(
                service.getClientServerPermissionsByUsername("fjwjfjwfwpkfwpkfp[wk", "yahya"),
                List.of(Permission.CREATE_ROOM, Permission.DELETE_ROOM)
        );
    }


}
