package com.financialplanner.moduleapi.controllers;

import com.financialplanner.moduleapi.dtos.auth.*;
import com.financialplanner.moduleapi.response.ApiResponse;
import com.financialplanner.moduleapi.response.ApiResponseFactory;
import com.financialplanner.moduleapi.security.JwtService;
import com.financialplanner.moduleauth.application.service.RefreshTokenServiceImpl;
import com.financialplanner.moduleauth.domain.service.AuthService;
import com.financialplanner.moduleauth.infrastructure.persistence.entity.Role;
import com.financialplanner.moduleauth.infrastructure.persistence.entity.User;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final RefreshTokenServiceImpl refreshTokenService;
    private final JwtService jwtService;
    private final ApiResponseFactory responseFactory;

    public AuthController(AuthService authService, RefreshTokenServiceImpl refreshTokenService, JwtService jwtService,
                          ApiResponseFactory responseFactory) {
        this.authService         = authService;
        this.refreshTokenService = refreshTokenService;
        this.jwtService          = jwtService;
        this.responseFactory     = responseFactory;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {

        User user = authService.register(request.email(), request.password(), request.first(), request.last());

        var roleNames = user.getRoles()
                            .stream()
                            .map(Role::getName)
                            .toList();

        var accessToken = jwtService.generateToken(user.getEmail(), Map.of("roles", roleNames));

        var refreshToken = refreshTokenService.createRefreshToken(user.getId());

        var authResponse = new AuthResponse(accessToken, refreshToken.getToken(), user.getId(), user.getEmail(),
                                            user.getUserId(), user.getFirst(), user.getLast(), Set.copyOf(roleNames));

        return ResponseEntity.ok(responseFactory.success(authResponse, "Registration successful"));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {

        User user = authService.login(request.email(), request.password());

        var roleNames = user.getRoles()
                            .stream()
                            .map(Role::getName)
                            .toList();

        var accessToken = jwtService.generateToken(user.getEmail(), Map.of("roles", roleNames));

        var refreshToken = refreshTokenService.createRefreshToken(user.getId());

        var authResponse = new AuthResponse(accessToken, refreshToken.getToken(), user.getId(), user.getEmail(),
                                            user.getUserId(), user.getFirst(), user.getLast(), Set.copyOf(roleNames));

        return ResponseEntity.ok(responseFactory.success(authResponse, "Login successful"));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<Map<String, String>>> refresh(@RequestBody RefreshTokenRequest request) {

        var refreshToken = refreshTokenService.findByToken(request.refreshToken());
        refreshTokenService.verifyExpiration(refreshToken);

        var user = refreshToken.getUser();

        var roleNames = user.getRoles()
                            .stream()
                            .map(Role::getName)
                            .toList();

        var newAccessToken = jwtService.generateToken(user.getEmail(), Map.of("roles", roleNames));

        var newRefreshToken = refreshTokenService.createRefreshToken(user.getId());

        var tokens = Map.of("accessToken", newAccessToken, "refreshToken", newRefreshToken.getToken());

        return ResponseEntity.ok(responseFactory.success(tokens, "Token refreshed"));
    }

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<AuthResponse>> changePassword(
        @Valid @RequestBody ChangePasswordRequest request) {

        // Service call using the request object
        User user = authService.changePassword(
            request.userId(),
            request.currentPassword(),
            request.newPassword()
                                              );

        var roleNames = user.getRoles()
                            .stream()
                            .map(Role::getName)
                            .toList();

        var accessToken = jwtService.generateToken(user.getEmail(), Map.of("roles", roleNames));
        var refreshToken = refreshTokenService.createRefreshToken(user.getId());

        var authResponse = new AuthResponse(
            accessToken,
            refreshToken.getToken(),
            user.getId(),
            user.getEmail(),
            user.getUserId(),
            user.getFirst(),
            user.getLast(),
            Set.copyOf(roleNames)
        );

        return ResponseEntity.ok(
            responseFactory.success(authResponse, "Password changed successfully")
                                );
    }
}
