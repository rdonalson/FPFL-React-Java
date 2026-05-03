package com.financialplanner.moduleauth.infrastructure.persistence.repository;

import com.financialplanner.moduleauth.infrastructure.persistence.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JpaUserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}
