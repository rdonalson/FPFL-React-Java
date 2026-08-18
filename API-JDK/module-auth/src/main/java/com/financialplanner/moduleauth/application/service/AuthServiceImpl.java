package com.financialplanner.moduleauth.application.service;

import com.financialplanner.moduleauth.domain.service.AuthService;
import com.financialplanner.moduleauth.domain.service.RoleService;
import com.financialplanner.moduleauth.domain.service.UserRolesService;
import com.financialplanner.moduleauth.domain.service.UserService;
import com.financialplanner.moduleauth.infrastructure.persistence.entity.User;
import com.financialplanner.modulecommonbc.exception.InvalidCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserService userService;
    private final RoleService roleService;
    private final UserRolesService userRolesService;
    private final PasswordEncoder passwordEncoder;

    public AuthServiceImpl(UserService userService,
                           RoleService roleService,
                           UserRolesService userRolesService,
                           PasswordEncoder passwordEncoder) {
        this.userService       = userService;
        this.roleService       = roleService;
        this.userRolesService  = userRolesService;
        this.passwordEncoder   = passwordEncoder;
    }

    @Override
    @Transactional
    public User register(String email, String rawPassword, String first, String last) {

        // Ensure ROLE_USER exists
        var userRole = roleService.ensureRoleExists("ROLE_USER");

        // Create user (no roles assigned here)
        User user = userService.createUser(email, rawPassword, Set.of(userRole), first, last);

        // Persist role mapping in user_roles table
        userRolesService.addRoleToUser(user.getId(), userRole.getId());

        return user;
    }

    @Override
    public User login(String email, String rawPassword) {

        User user = userService.findByEmail(email)
            .orElseThrow(() -> new InvalidCredentialsException("Invalid credentials"));

        if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid credentials");
        }

        // Fetch DB-backed roles for login response or token creation
        List<String> roleNames = userRolesService.getRoleNamesForUser(user.getId());

        // Attach roles to the User object? No — your User entity does not contain roles.
        // Instead, your controller or token generator will use roleNames.

        return user;
    }

    @Override
    @Transactional
    public User changePassword(Long userId, String currentPassword, String newPassword) {

        User user = userService.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new InvalidCredentialsException("Current password is incorrect");
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));

        return userService.save(user);
    }

    @Override
    public User loadUserByEmail(String email) {
        return userService.findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }
}
