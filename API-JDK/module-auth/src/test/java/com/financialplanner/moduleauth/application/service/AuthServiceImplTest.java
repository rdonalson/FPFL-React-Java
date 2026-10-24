package com.financialplanner.moduleauth.application.service;

import com.financialplanner.modulecommonbc.exception.InvalidCredentialsException;
import com.financialplanner.moduleauth.domain.service.RoleService;
import com.financialplanner.moduleauth.domain.service.UserRolesService;
import com.financialplanner.moduleauth.domain.service.UserService;
import com.financialplanner.moduleauth.infrastructure.persistence.entity.Role;
import com.financialplanner.moduleauth.infrastructure.persistence.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuthServiceImplTest {

    private final UserService users = mock(UserService.class);
    private final RoleService roles = mock(RoleService.class);
    private final UserRolesService userRoles = mock(UserRolesService.class);
    private final PasswordEncoder encoder = new BCryptPasswordEncoder(4); // fast rounds for tests
    private AuthServiceImpl service;

    private User rick;

    @BeforeEach
    void setUp() {
        service = new AuthServiceImpl(users, roles, userRoles, encoder);
        rick = new User();
        rick.setId(7L);
        rick.setEmail("rick@example.com");
        rick.setPasswordHash(encoder.encode("Password@2"));
    }

    @Test
    void registerCreatesTheUserWithRoleUser() {
        Role roleUser = new Role("ROLE_USER");
        roleUser.setId(2L);
        when(roles.ensureRoleExists("ROLE_USER")).thenReturn(roleUser);
        when(users.createUser("new@example.com", "secret", Set.of(roleUser), "New", "User")).thenReturn(rick);

        User created = service.register("new@example.com", "secret", "New", "User");

        assertSame(rick, created);
        verify(userRoles).addRoleToUser(7L, 2L);
    }

    @Test
    void loginAcceptsTheRightPassword() {
        when(users.findByEmail("rick@example.com")).thenReturn(Optional.of(rick));
        when(userRoles.getRoleNamesForUser(7L)).thenReturn(List.of("ROLE_USER"));

        assertSame(rick, service.login("rick@example.com", "Password@2"));
    }

    @Test
    void loginRejectsAWrongPasswordOrUnknownEmailWithTheSameMessage() {
        when(users.findByEmail("rick@example.com")).thenReturn(Optional.of(rick));
        when(users.findByEmail("nobody@example.com")).thenReturn(Optional.empty());

        var wrong = assertThrows(InvalidCredentialsException.class, () -> service.login("rick@example.com", "nope"));
        var unknown = assertThrows(InvalidCredentialsException.class, () -> service.login("nobody@example.com", "x"));
        assertEquals("Invalid credentials", wrong.getMessage());
        assertEquals(wrong.getMessage(), unknown.getMessage()); // no hint about which part was wrong
    }

    @Test
    void changePasswordChecksTheCurrentOneAndStoresAHash() {
        when(users.findById(7L)).thenReturn(Optional.of(rick));
        when(users.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User saved = service.changePassword(7L, "Password@2", "NewPassword@3");

        assertNotEquals("NewPassword@3", saved.getPasswordHash());
        assertTrue(encoder.matches("NewPassword@3", saved.getPasswordHash()));
    }

    @Test
    void changePasswordRefusesAWrongCurrentPasswordOrUnknownUser() {
        when(users.findById(7L)).thenReturn(Optional.of(rick));
        when(users.findById(8L)).thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class, () -> service.changePassword(7L, "wrong", "x"));
        assertThrows(IllegalArgumentException.class, () -> service.changePassword(8L, "a", "b"));
        verify(users, never()).save(any());
    }

    @Test
    void loadUserByEmail() {
        when(users.findByEmail("rick@example.com")).thenReturn(Optional.of(rick));
        assertSame(rick, service.loadUserByEmail("rick@example.com"));
        assertThrows(IllegalArgumentException.class, () -> service.loadUserByEmail("missing@example.com"));
    }
}
