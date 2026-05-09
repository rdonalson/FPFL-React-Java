package com.financialplanner.moduleapi.dtos.auth;

import java.util.Set;
import java.util.UUID;

public record AuthResponse(
    String accessToken,
    String refreshToken,
    Long id,
    String email,
    UUID userID,
    String first,
    String last,
    Set<String> roles
) {}
