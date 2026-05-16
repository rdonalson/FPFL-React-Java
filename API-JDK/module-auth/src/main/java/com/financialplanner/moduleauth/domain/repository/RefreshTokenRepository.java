package com.financialplanner.moduleauth.domain.repository;

import com.financialplanner.moduleauth.infrastructure.persistence.entity.RefreshToken;
import com.financialplanner.moduleauth.infrastructure.persistence.entity.User;

import java.util.Optional;

public interface RefreshTokenRepository {
    Optional<RefreshToken> findByToken(String token);
    RefreshToken save(RefreshToken token);
    void delete(RefreshToken token);
    void deleteByUser(User user);
}
