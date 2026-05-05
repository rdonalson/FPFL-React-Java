package com.financialplanner.moduleapi.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.Map;

@Service
public class JwtService {

    private final Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    private final long accessTokenExpirationMs; // 15 minutes

    public JwtService() {
        accessTokenExpirationMs = 1000 * 60 * 15;
    }

    public String generateToken(String email, Map<String, Object> claims) {
        return Jwts.builder()
                   .setClaims(claims)
                   .setSubject(email)
                   .setIssuedAt(new Date())
                   .setExpiration(new Date(System.currentTimeMillis() + accessTokenExpirationMs))
                   .signWith(key)
                   .compact();
    }

    public String extractEmail(String token) {
        return extractAllClaims(token).getSubject();
    }

    public boolean isTokenValid(String token) {
        try {
            extractAllClaims(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                   .setSigningKey(key)
                   .build()
                   .parseClaimsJws(token)
                   .getBody();
    }
}
