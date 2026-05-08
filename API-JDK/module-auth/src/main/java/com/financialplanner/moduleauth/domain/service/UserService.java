package com.financialplanner.moduleauth.domain.service;

import com.financialplanner.moduleauth.infrastructure.persistence.entity.User;

import java.util.Optional;

public interface UserService {
    Optional<User> findByEmail(String email);
    Optional<User> findById(Long id);
    User createUser(String email, String rawPassword, java.util.Set<com.financialplanner.moduleauth.infrastructure.persistence.entity.Role> roles);
    User save(User user);
    void deleteById(Long id);
}
