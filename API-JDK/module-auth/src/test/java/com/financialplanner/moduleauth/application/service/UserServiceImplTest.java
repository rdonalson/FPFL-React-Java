package com.financialplanner.moduleauth.application.service;

import com.financialplanner.moduleauth.domain.repository.UserRepository;
import com.financialplanner.moduleauth.infrastructure.persistence.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserServiceImplTest {

    private final UserRepository repo = mock(UserRepository.class);
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(4);
    private final UserServiceImpl service = new UserServiceImpl(repo, encoder);

    @Test
    void createUserHashesThePasswordAndKeepsTheName() {
        when(repo.findByEmail("new@example.com")).thenReturn(Optional.empty());
        when(repo.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User user = service.createUser("new@example.com", "secret", Set.of(), "New", "User");

        assertEquals("new@example.com", user.getEmail());
        assertEquals("New", user.getFirst());
        assertEquals("User", user.getLast());
        assertNotEquals("secret", user.getPasswordHash());
        assertTrue(encoder.matches("secret", user.getPasswordHash()));
        assertNotNull(user.getUserId()); // a UUID is assigned up front
    }

    @Test
    void anEmailCanOnlyRegisterOnce() {
        when(repo.findByEmail("rick@example.com")).thenReturn(Optional.of(new User()));
        var ex = assertThrows(IllegalArgumentException.class,
                              () -> service.createUser("rick@example.com", "x", Set.of(), "R", "D"));
        assertEquals("Email already registered", ex.getMessage());
        verify(repo, never()).save(any());
    }

    @Test
    void lookupsAndSaveDelegateToTheRepository() {
        User u = new User();
        UUID id = UUID.randomUUID();
        when(repo.findByEmail("a@b.c")).thenReturn(Optional.of(u));
        when(repo.findById(1L)).thenReturn(Optional.of(u));
        when(repo.findByUserId(id)).thenReturn(Optional.of(u));
        when(repo.save(u)).thenReturn(u);

        assertSame(u, service.findByEmail("a@b.c").orElseThrow());
        assertSame(u, service.findById(1L).orElseThrow());
        assertSame(u, service.findByUserId(id).orElseThrow());
        assertSame(u, service.save(u));
    }
}
