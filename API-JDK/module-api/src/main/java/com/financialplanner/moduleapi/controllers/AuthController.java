package com.financialplanner.moduleapi.controllers;

import com.financialplanner.moduleapi.dtos.auth.AuthResponse;
import com.financialplanner.moduleapi.dtos.auth.LoginRequest;
import com.financialplanner.moduleapi.dtos.auth.RefreshTokenRequest;
import com.financialplanner.moduleapi.dtos.auth.RegisterRequest;
import com.financialplanner.moduleapi.security.JwtService;
import com.financialplanner.moduleauth.application.service.RefreshTokenServiceImpl;
import com.financialplanner.moduleauth.domain.service.AuthService;
import com.financialplanner.moduleauth.infrastructure.persistence.entity.Role;
import com.financialplanner.moduleauth.infrastructure.persistence.entity.User;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final RefreshTokenServiceImpl refreshTokenService;
    private final JwtService jwtService;

    public AuthController(AuthService authService, RefreshTokenServiceImpl refreshTokenService, JwtService jwtService) {
        this.authService         = authService;
        this.refreshTokenService = refreshTokenService;
        this.jwtService          = jwtService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {

        User user = authService.register(request.email(), request.password());

        // Build roles as a list of names for JWT claims
        var roleNames = user.getRoles()
                            .stream()
                            .map(Role::getName)
                            .toList();

        var accessToken = jwtService.generateToken(user.getEmail(), Map.of("roles", roleNames));

        var refreshToken = refreshTokenService.createRefreshToken(user.getId());

        Set<String> roles = roleNames.stream()
                                     .collect(Collectors.toSet());

        var response = new AuthResponse(accessToken, refreshToken.getToken(), user.getId(), user.getEmail(), roles);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {

        // Authenticate user (AuthServiceImpl.login checks password)
        User user = authService.login(request.email(), request.password());

        // Map roles to simple names for JWT claims
        var roleNames = user.getRoles()
                            .stream()
                            .map(Role::getName)
                            .toList();

        // Generate access token with roles claim as a list of strings
        var accessToken = jwtService.generateToken(user.getEmail(), Map.of("roles", roleNames));

        // Create refresh token (persisted UUID)
        var refreshToken = refreshTokenService.createRefreshToken(user.getId());

        Set<String> roles = roleNames.stream()
                                     .collect(Collectors.toSet());

        var response = new AuthResponse(accessToken, refreshToken.getToken(), user.getId(), user.getEmail(), roles);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public Map<String, String> refresh(@RequestBody RefreshTokenRequest request) {

        // before: var refreshToken = refreshTokenService.findByToken(request.getRefreshToken());
        var refreshToken = refreshTokenService.findByToken(request.refreshToken());

        refreshTokenService.verifyExpiration(refreshToken);

        var user = refreshToken.getUser();

        var roleNames = user.getRoles().stream()
                            .map(Role::getName)
                            .toList();

        var newAccessToken = jwtService.generateToken(user.getEmail(), Map.of("roles", roleNames));

        var newRefreshToken = refreshTokenService.createRefreshToken(user.getId());

        return Map.of("accessToken", newAccessToken, "refreshToken", newRefreshToken.getToken());
    }
}
