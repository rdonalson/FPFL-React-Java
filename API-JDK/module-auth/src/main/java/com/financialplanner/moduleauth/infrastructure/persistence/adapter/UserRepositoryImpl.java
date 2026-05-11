package com.financialplanner.moduleauth.infrastructure.persistence.adapter;

import com.financialplanner.moduleauth.domain.repository.UserRepository;
import com.financialplanner.moduleauth.infrastructure.persistence.entity.User;
import com.financialplanner.moduleauth.infrastructure.persistence.repository.JpaUserRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class UserRepositoryImpl implements UserRepository {

    private final JpaUserRepository jpa;

    public UserRepositoryImpl(JpaUserRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return jpa.findByEmail(email);
    }

    @Override
    public Optional<User> findById(Long id) {
        return jpa.findById(id);
    }

    @Override
    public Optional<User> findByUserId(UUID userId) {
        return jpa.findByUserId(userId);
    }

    @Override
    public User save(User user) {
        return jpa.save(user);
    }
}
