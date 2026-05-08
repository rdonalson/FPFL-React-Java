package com.financialplanner.moduleapi.controllers;

import com.financialplanner.moduleapi.dtos.auth.AuthResponse;
import com.financialplanner.moduleapi.dtos.auth.RefreshTokenRequest;
import com.financialplanner.moduleapi.dtos.auth.RegisterRequest;
import com.financialplanner.moduleapi.security.JwtService;
import com.financialplanner.moduleauth.application.service.RefreshTokenServiceImpl;
import com.financialplanner.moduleauth.domain.service.AuthService;
import com.financialplanner.moduleauth.infrastructure.persistence.entity.Role;
import com.financialplanner.moduleauth.infrastructure.persistence.entity.User;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final RefreshTokenServiceImpl refreshTokenService;
    private final JwtService jwtService;

    public AuthController(AuthService authService,
                          RefreshTokenServiceImpl refreshTokenService,
                          JwtService jwtService) {
        this.authService = authService;
        this.refreshTokenService = refreshTokenService;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {

        // create user (AuthServiceImpl.register handles duplicate email + password encoding)
        User user = authService.register(request.email(), request.password());

        // create tokens
        var accessToken = jwtService.generateToken(user.getEmail(),
                                                   user.getRoles().stream().map(Role::getName).collect(Collectors.toSet())
                                                       .stream().collect(Collectors.toMap(role -> "roles", role -> user.getRoles())));

        var refreshToken = refreshTokenService.createRefreshToken(user.getId());

        Set<String> roles = user.getRoles().stream().map(Role::getName).collect(Collectors.toSet());

        var response = new AuthResponse(accessToken, refreshToken.getToken(), user.getId(), user.getEmail(), roles);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public Map<String, String> refresh(@RequestBody RefreshTokenRequest request) {

        var refreshToken = refreshTokenService.findByToken(request.getRefreshToken());

        refreshTokenService.verifyExpiration(refreshToken);

        var user = refreshToken.getUser();

        // Map roles to a list of role names for JWT claims
        var roleNames = user.getRoles().stream()
                            .map(Role::getName)
                            .toList();

        var newAccessToken = jwtService.generateToken(user.getEmail(), Map.of("roles", roleNames));

        // Optional: rotate refresh token
        var newRefreshToken = refreshTokenService.createRefreshToken(user.getId());

        return Map.of("accessToken", newAccessToken, "refreshToken", newRefreshToken.getToken());
    }
}
