package service;

import org.example.Permission;
import org.example.entity.Role;
import org.example.entity.RoleUser;
import org.example.repository.RoleRepository;
import org.example.repository.RoleUserRepository;
import org.example.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class) 
public class RoleAssignmentTest {
    @Mock
    RoleRepository repository;

    @Mock
    UserService userService;
    @Mock
    RoleUserService roleUserService;

    @Mock
    RoleUserRepository roleUserRepository;

    @Mock
    ServerService serverService;

    @Mock
    ServerNotificationService serverNotificationService;

    @InjectMocks
    RoleAssignmentService roleAssignmentService;

    private String mockServerId;
    private Role mockRole1;
    private Role mockRole2;
    private Role mockRole3;
    private Role mockRole4;

    private RoleUser mockRoleUser3_1;
    private RoleUser mockRoleUser3_2;
    private RoleUser mockRoleUser3_3;
    private RoleUser mockRoleUser1_1;
    private RoleUser mockRoleUser4_1;
    private RoleUser mockRoleUser2_1;

    @BeforeEach
    void setup(){
        this.mockServerId = UUID.randomUUID().toString();
        this.mockRole1 = Role.builder()
                .id(UUID.randomUUID().toString())
                .name("role1")
                .ranking(2)
                .serverId(this.mockServerId)
                .permissions(List.of(Permission.ASSIGN_ROLE, Permission.CREATE_ROLES))
                .build();
        this.mockRole2 = Role.builder()
                .id(UUID.randomUUID().toString())
                .name("role2")
                .ranking(1)
                .serverId(this.mockServerId)
                .permissions(List.of(Permission.ASSIGN_ROLE, Permission.CREATE_ROLES))
                .build();
        this.mockRole3 = Role.builder()
                .id(UUID.randomUUID().toString())
                .name("role3")
                .ranking(3)
                .serverId(this.mockServerId)
                .permissions(List.of(Permission.ASSIGN_ROLE, Permission.CREATE_ROLES))
                .build();
        this.mockRole4 = Role.builder()
                .id(UUID.randomUUID().toString())
                .name("role4")
                .ranking(4)
                .serverId(this.mockServerId)
                .permissions(List.of(Permission.ASSIGN_ROLE, Permission.CREATE_ROLES))
                .build();

        this.mockRoleUser3_1 = RoleUser.builder()
                .id(UUID.randomUUID().toString())
                .role(this.mockRole3)
                .username("yahya")
                .build();

        this.mockRoleUser3_2 = RoleUser.builder()
                .id(UUID.randomUUID().toString())
                .role(this.mockRole3)
                .username("ahmed")
                .build();

        this.mockRoleUser3_3 = RoleUser.builder()
                .id(UUID.randomUUID().toString())
                .role(this.mockRole3)
                .username("badr")
                .build();

        this.mockRoleUser1_1 = RoleUser.builder()
                .id(UUID.randomUUID().toString())
                .role(this.mockRole1)
                .username("yahya")
                .build();

        this.mockRoleUser4_1 = RoleUser.builder()
                .id(UUID.randomUUID().toString())
                .role(this.mockRole4)
                .username("ahmed")
                .build();

        this.mockRoleUser2_1 =RoleUser.builder()
                .id(UUID.randomUUID().toString())
                .role(this.mockRole2)
                .username("badr")
                .build();
    }

    @Test
    void testGetRoleMainMembers(){
        when(roleUserRepository.findByRoleId(mockRole3.getId()))
                .thenReturn(List.of(this.mockRoleUser3_1, this.mockRoleUser3_2, this.mockRoleUser3_3));

        when(roleUserRepository.findByUsernameAndRole_ServerId(this.mockRoleUser3_1.getUsername(), this.mockRoleUser3_1.getRole().getServerId()))
                .thenReturn(List.of(this.mockRoleUser3_1, this.mockRoleUser1_1));

        when(roleUserRepository.findByUsernameAndRole_ServerId(this.mockRoleUser3_2.getUsername(), this.mockRoleUser3_2.getRole().getServerId()))
                .thenReturn(List.of(this.mockRoleUser3_2, this.mockRoleUser4_1));

        when(roleUserRepository.findByUsernameAndRole_ServerId(this.mockRoleUser3_3.getUsername(), this.mockRoleUser3_3.getRole().getServerId()))
                .thenReturn(List.of(this.mockRoleUser3_3, this.mockRoleUser2_1));

        //assertEquals(roleAssignmentService.getRoleMainMembers(this.mockRole3), List.of("ahmed"));//?
    }
}
