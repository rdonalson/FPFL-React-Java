package com.financialplanner.moduleauth.domain.service;

import com.financialplanner.moduleauth.infrastructure.persistence.entity.Role;
import com.financialplanner.moduleauth.infrastructure.persistence.entity.User;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface UserService {

    Optional<User> findByEmail(String email);

    Optional<User> findById(Long id);

    Optional<User> findByUserId(UUID userId);

    User createUser(String email, String rawPassword, Set<Role> roles, String first, String last);

    User save(User user);
}
