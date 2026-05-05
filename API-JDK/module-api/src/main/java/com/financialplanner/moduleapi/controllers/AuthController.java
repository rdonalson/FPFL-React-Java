package com.financialplanner.moduleapi.controllers;

import com.financialplanner.moduleapi.dtos.auth.RefreshTokenRequest;
import com.financialplanner.moduleapi.security.JwtService;
import com.financialplanner.moduleauth.application.service.RefreshTokenServiceImpl;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final RefreshTokenServiceImpl refreshTokenService;
    private final JwtService jwtService;

    public AuthController(RefreshTokenServiceImpl refreshTokenService, JwtService jwtService) {

        this.refreshTokenService = refreshTokenService;
        this.jwtService          = jwtService;
    }

    @PostMapping("/refresh")
    public Map<String, String> refresh(@RequestBody RefreshTokenRequest request) {

        var refreshToken = refreshTokenService.findByToken(request.getRefreshToken());

        refreshTokenService.verifyExpiration(refreshToken);

        var user = refreshToken.getUser();

        var newAccessToken = jwtService.generateToken(user.getEmail(), Map.of("roles", user.getRoles()));

        // Optional: rotate refresh token
        var newRefreshToken = refreshTokenService.createRefreshToken(user.getId());

        return Map.of("accessToken", newAccessToken, "refreshToken", newRefreshToken.getToken());
    }

}
