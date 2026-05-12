package com.financialplanner.moduleauth.application.service;

import com.financialplanner.moduleauth.domain.service.AuthService;
import com.financialplanner.moduleauth.domain.service.RoleService;
import com.financialplanner.moduleauth.domain.service.UserService;
import com.financialplanner.moduleauth.infrastructure.persistence.entity.User;
import com.financialplanner.modulecommonbc.exception.InvalidCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserService userService;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder; // optional if userService handles encoding

    public AuthServiceImpl(UserService userService, RoleService roleService, PasswordEncoder passwordEncoder) {
        this.userService     = userService;
        this.roleService     = roleService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public User register(String email, String rawPassword, String first, String last) {

        // RoleService ensures ROLE_USER exists
        var userRole = roleService.ensureRoleExists("ROLE_USER");

        // Delegate creation to UserService (which handles duplicate check + encoding)
        return userService.createUser(email, rawPassword, Set.of(userRole), first, last);
    }

    @Override
    public User login(String email, String rawPassword) {

        User user = userService.findByEmail(email)
                               .orElseThrow(() -> new InvalidCredentialsException("Invalid credentials"));

        if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid credentials");
        }

        return user;
    }

    @Override
    public User loadUserByEmail(String email) {
        return userService.findByEmail(email)
                          .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }
}
