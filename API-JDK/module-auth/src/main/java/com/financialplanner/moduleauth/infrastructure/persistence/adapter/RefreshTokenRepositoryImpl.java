package com.financialplanner.moduleauth.infrastructure.persistence.adapter;

import com.financialplanner.moduleauth.domain.repository.RefreshTokenRepository;
import com.financialplanner.moduleauth.infrastructure.persistence.entity.RefreshToken;
import com.financialplanner.moduleauth.infrastructure.persistence.entity.User;
import com.financialplanner.moduleauth.infrastructure.persistence.repository.JpaRefreshTokenRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class RefreshTokenRepositoryImpl implements RefreshTokenRepository {

    private final JpaRefreshTokenRepository jpa;

    public RefreshTokenRepositoryImpl(JpaRefreshTokenRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Optional<RefreshToken> findByToken(String token) {
        return jpa.findByToken(token);
    }

    @Override
    public RefreshToken save(RefreshToken token) {
        return jpa.save(token);
    }

    @Override
    public void delete(RefreshToken token) {
        jpa.delete(token);
    }

    @Override
    public void deleteByUser(User user) {
        jpa.deleteByUser(user);
    }
}
