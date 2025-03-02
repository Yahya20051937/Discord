package service;

import org.example.Permission;
import org.example.ScopeType;
import org.example.entity.Role;
import org.example.entity.RoleUser;
import org.example.entity.RoomScope;
import org.example.repository.RoleRepository;
import org.example.repository.RoleUserRepository;
import org.example.repository.RoomScopeRepository;
import org.example.service.RoleUserService;
import org.example.service.RoomScopeService;
import org.example.service.ServerService;
import org.example.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class RoomScopeTest {
    @Mock
    RoleUserRepository roleUserRepository;

    @Mock
    RoleRepository roleRepository;

    @Mock
    RoleUserService roleUserService;


    @Mock
    UserService userService;

    @Mock
    ServerService serverService;

    @Mock
    RoomScopeRepository repository;

    @InjectMocks
    RoomScopeService roomScopeService;

    private RoleUser mockRoleUser1;
    private RoleUser mockRoleUser2;

    private String serverId;

    private Role mockRole1;
    private Role mockRole2;
    private Role mockRole3;

    private RoleUser mockRoleUser3;

    private String mockRoom1Id;
    private String mockRoom2Id;

    private RoomScope mockRoomScope1;
    private RoomScope mockRoomScope2;
    private RoomScope mockRoomScope3;
    private RoomScope mockRoomScope4;

    @BeforeEach
    void setUp(){
        MockitoAnnotations.openMocks(this);
        this.serverId = UUID.randomUUID().toString();

        this.mockRoom1Id = UUID.randomUUID().toString();
        this.mockRoom2Id = UUID.randomUUID().toString();

        this.mockRoomScope1 = RoomScope.builder()
                .id(UUID.randomUUID().toString())
                .roomId(this.mockRoom1Id)
                .scopeType(ScopeType.JOIN)
                .build();


        this.mockRoomScope2 = RoomScope.builder()
                .id(UUID.randomUUID().toString())
                .roomId(this.mockRoom1Id)
                .scopeType(ScopeType.READ)
                .build();

        this.mockRoomScope3 = RoomScope.builder()
                .id(UUID.randomUUID().toString())
                .roomId(this.mockRoom2Id)
                .scopeType(ScopeType.JOIN)
                .build();

        this.mockRoomScope4 = RoomScope.builder()
                .id(UUID.randomUUID().toString())
                .roomId(this.mockRoom2Id)
                .scopeType(ScopeType.WRITE)
                .build();

        this.mockRole1 = Role.builder()
                .id(UUID.randomUUID().toString())
                .name("admin")
                .ranking(3)
                .serverId(this.serverId)
                .permissions(List.of(Permission.ASSIGN_ROLE, Permission.CREATE_ROLES))
                .roomsScopes(List.of(mockRoomScope1, mockRoomScope3))
                .build();
        this.mockRole2 = Role.builder()
                .id(UUID.randomUUID().toString())
                .name("admin")
                .ranking(1)
                .serverId(this.serverId)
                .permissions(List.of(Permission.CREATE_ROOM, Permission.DELETE_ROOM))
                .roomsScopes(List.of(mockRoomScope2, mockRoomScope4))
                .build();

        this.mockRole3 = Role.builder()
                .id(UUID.randomUUID().toString())
                .name("admin")
                .ranking(1)
                .serverId("fjwjfjwfwpkfwpkfp[wk")
                .permissions(List.of(Permission.CREATE_ROOM, Permission.DELETE_ROOM))
                .roomsScopes(List.of(mockRoomScope2, mockRoomScope4))
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
                .role(this.mockRole3)
                .username("yahya")
                .build();

        this.mockRoomScope1.setRole(this.mockRole1);
        this.mockRoomScope2.setRole(this.mockRole2);
        this.mockRoomScope4.setRole(this.mockRole1);

    }

    @Test
    void testDoesUserHaveScopeInRoom(){
        when(roleUserRepository.findByUsernameAndRole_ServerId("yahya", this.serverId))
                .thenReturn(List.of(this.mockRoleUser1));

        when(roleUserRepository.findByUsernameAndRole_ServerId("anouar", this.serverId))
                .thenReturn(List.of(this.mockRoleUser2));

        assertTrue(
                roomScopeService.testDoesUserHaveScopeInRoom("yahya", ScopeType.JOIN, this.mockRoom1Id, this.serverId)
        );

        assertFalse(
                roomScopeService.testDoesUserHaveScopeInRoom("yahya", ScopeType.WRITE, this.mockRoom2Id, this.serverId)
        );

        assertTrue(
                roomScopeService.testDoesUserHaveScopeInRoom("anouar", ScopeType.WRITE, this.mockRoom2Id, this.serverId)
        );

        assertFalse(
                roomScopeService.testDoesUserHaveScopeInRoom("anouar", ScopeType.JOIN, this.mockRoom2Id, this.serverId)
        );
    }

    @Test
    void testGetRoomScopes(){
        when(roleUserRepository.findByUsername("yahya"))
                .thenReturn(List.of(this.mockRoleUser1, this.mockRoleUser3));

        when(repository.findByRoomIdAndRoleId(this.mockRoom1Id, this.mockRole1.getId()))
                .thenReturn(List.of(this.mockRoomScope1));

        assertEquals(
                roomScopeService.getRoomScopes(this.mockRoom1Id, "yahya"),
                List.of(ScopeType.JOIN)
        );
    }




}
