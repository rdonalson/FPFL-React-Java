package com.financialplanner.moduleauth.domain.repository;

import com.financialplanner.moduleauth.infrastructure.persistence.entity.User;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository {
    Optional<User> findByEmail(String email);

    Optional<User> findById(Long id);

    Optional<User> findByUserID(UUID userID);

    User save(User user);
}
