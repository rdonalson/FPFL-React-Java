package com.financialplanner.moduleapi.security;

import java.util.Map;

public interface JwtService {

    String extractUsername(String token);

    String generateToken(String subject, Map<String, Object> claims);

    boolean isTokenValid(String token, String subject);
}
