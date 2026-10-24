package com.financialplanner.moduleauth.application.service;

import com.financialplanner.moduleauth.config.TokenProperties;
import com.financialplanner.moduleauth.domain.repository.RefreshTokenRepository;
import com.financialplanner.moduleauth.domain.service.UserService;
import com.financialplanner.moduleauth.infrastructure.persistence.entity.RefreshToken;
import com.financialplanner.moduleauth.infrastructure.persistence.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class RefreshTokenServiceImplTest {

    private final RefreshTokenRepository repo = mock(RefreshTokenRepository.class);
    private final UserService users = mock(UserService.class);
    private RefreshTokenServiceImpl service;
    private final User rick = new User();

    @BeforeEach
    void setUp() {
        TokenProperties props = new TokenProperties();
        props.setRefreshTokenExpirationMs(Duration.ofMinutes(15).toMillis());
        service = new RefreshTokenServiceImpl(repo, users, props);
        rick.setId(7L);
        when(repo.save(any(RefreshToken.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void createsAUniqueTokenThatExpiresAfterTheConfiguredTime() {
        when(users.findById(7L)).thenReturn(Optional.of(rick));
        when(repo.findByToken(anyString())).thenReturn(Optional.of(new RefreshToken()), Optional.empty());

        Instant before = Instant.now();
        RefreshToken token = service.createRefreshToken(7L);

        assertSame(rick, token.getUser());
        assertDoesNotThrow(() -> UUID.fromString(token.getToken()));
        assertTrue(token.getExpiryDate().isAfter(before.plus(Duration.ofMinutes(14))));
        assertTrue(token.getExpiryDate().isBefore(before.plus(Duration.ofMinutes(16))));
        verify(repo, times(2)).findByToken(anyString()); // retried after a (simulated) collision
    }

    @Test
    void noTokenForAnUnknownUser() {
        when(users.findById(9L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> service.createRefreshToken(9L));
    }

    @Test
    void anExpiredTokenIsDeletedAndRejected() {
        RefreshToken expired = new RefreshToken();
        expired.setExpiryDate(Instant.now().minusSeconds(1));

        var ex = assertThrows(IllegalArgumentException.class, () -> service.verifyExpiration(expired));
        assertEquals("Refresh token expired", ex.getMessage());
        verify(repo).delete(expired);
    }

    @Test
    void aLiveTokenPasses() {
        RefreshToken live = new RefreshToken();
        live.setExpiryDate(Instant.now().plusSeconds(60));
        assertDoesNotThrow(() -> service.verifyExpiration(live));
        verify(repo, never()).delete(any());
    }

    @Test
    void findByTokenAndDeleteByUser() {
        RefreshToken t = new RefreshToken();
        when(repo.findByToken("abc")).thenReturn(Optional.of(t));
        when(repo.findByToken("nope")).thenReturn(Optional.empty());

        assertSame(t, service.findByToken("abc"));
        assertThrows(IllegalArgumentException.class, () -> service.findByToken("nope"));
        service.deleteByUser(rick);
        verify(repo).deleteByUser(rick);
    }
}
