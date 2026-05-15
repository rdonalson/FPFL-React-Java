package com.financialplanner.moduleapi.dtos.auth;

import com.financialplanner.modulecommonbc.sanitizer.NoSanitize;

import java.util.Set;
import java.util.UUID;

public record AuthResponse(
    String accessToken,
    String refreshToken,
    Long id,
    String email,
    UUID userId,
    String first,
    String last,
    Set<String> roles
) implements NoSanitize {}
