package com.financialplanner.moduleapi.dtos.auth;

import java.util.Set;

public record AuthResponse(
    String accessToken,
    String refreshToken,
    Long userId,
    String email,
    Set<String> roles
) {}
