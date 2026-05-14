package com.financialplanner.moduleauth.application.service;

import com.financialplanner.moduleauth.config.TokenProperties;
import com.financialplanner.moduleauth.domain.repository.RefreshTokenRepository;
import com.financialplanner.moduleauth.domain.service.RefreshTokenService;
import com.financialplanner.moduleauth.domain.service.UserService;
import com.financialplanner.moduleauth.infrastructure.persistence.entity.RefreshToken;
import com.financialplanner.moduleauth.infrastructure.persistence.entity.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserService userService;

    private final long refreshTokenDurationMs;

    public RefreshTokenServiceImpl(RefreshTokenRepository refreshTokenRepository, UserService userService,
                                   TokenProperties tokenProperties) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.userService            = userService;
        this.refreshTokenDurationMs = tokenProperties.getRefreshTokenExpirationMs();
    }

    @Override
    @Transactional
    public RefreshToken createRefreshToken(Long userId) {

        User user = userService.findById(userId)
                               .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Ensure token uniqueness (extremely unlikely collision, but safe)
        String tokenValue;
        Optional<RefreshToken> existing;
        do {
            tokenValue = UUID.randomUUID()
                             .toString();
            existing   = refreshTokenRepository.findByToken(tokenValue);
        } while (existing.isPresent());

        RefreshToken token = new RefreshToken();
        token.setUser(user);
        token.setToken(tokenValue);
        token.setExpiryDate(Instant.now()
                                   .plusMillis(refreshTokenDurationMs));

        return refreshTokenRepository.save(token);
    }

    @Override
    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate()
                 .isBefore(Instant.now())) {
            refreshTokenRepository.delete(token);
            throw new IllegalArgumentException("Refresh token expired");
        }
        return token;
    }

    @Override
    @Transactional
    public void deleteByUser(User user) {
        refreshTokenRepository.deleteByUser(user);
    }

    @Override
    public RefreshToken findByToken(String token) {
        return refreshTokenRepository.findByToken(token)
                                     .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));
    }
}
