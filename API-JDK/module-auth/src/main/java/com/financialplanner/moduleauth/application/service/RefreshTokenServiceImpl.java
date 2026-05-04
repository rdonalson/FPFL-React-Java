package com.financialplanner.moduleauth.application.service;


import com.financialplanner.moduleauth.domain.repository.RefreshTokenRepository;
import com.financialplanner.moduleauth.domain.repository.UserRepository;
import com.financialplanner.moduleauth.domain.service.RefreshTokenService;
import com.financialplanner.moduleauth.infrastructure.persistence.entity.RefreshToken;
import com.financialplanner.moduleauth.infrastructure.persistence.entity.User;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    // 7 days
    private final long refreshTokenDurationMs;

    public RefreshTokenServiceImpl(
        RefreshTokenRepository refreshTokenRepository,
        UserRepository userRepository
                                  ) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository         = userRepository;
        refreshTokenDurationMs      = 7 * 24 * 60 * 60 * 1000L;
    }

    @Override
    public RefreshToken createRefreshToken(Long userId) {

        User user = userRepository.findById(userId)
                                  .orElseThrow(() -> new IllegalArgumentException("User not found"));

        RefreshToken token = new RefreshToken();
        token.setUser(user);
        token.setToken(UUID.randomUUID().toString());
        token.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));

        return refreshTokenRepository.save(token);
    }

    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate()
                 .isBefore(Instant.now())) {
            refreshTokenRepository.delete(token);
            throw new IllegalArgumentException("Refresh token expired");
        }
        return token;
    }

    public void deleteByUser(User user) {
        refreshTokenRepository.deleteByUser(user);
    }

    public RefreshToken findByToken(String token) {
        return refreshTokenRepository.findByToken(token)
                                     .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));
    }
}

