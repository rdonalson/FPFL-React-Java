package com.financialplanner.moduleapi.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import org.junit.jupiter.api.Test;

import com.financialplanner.moduleauth.config.TokenProperties;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceImplTest {

    static final String SECRET = "dGVzdC1vbmx5LWp3dC1zZWNyZXQtdGVzdC1vbmx5LWp3dC1zZWNyZXQtMTIzNDU2";

    /** A service signing with {@link #SECRET} and tokens that live {@code expirationMs}. */
    static JwtServiceImpl service(long expirationMs) {
        return new JwtServiceImpl(properties(SECRET, expirationMs));
    }

    static TokenProperties properties(String secret, long expirationMs) {
        TokenProperties p = new TokenProperties();
        p.setSecret(secret);
        p.setAccessTokenExpirationMs(expirationMs);
        return p;
    }

    private final JwtServiceImpl jwt = service(900_000);

    @Test
    void aGeneratedTokenNamesItsSubjectAndIsValidForThem() {
        String token = jwt.generateToken("rick@example.com", Map.of("roles", List.of("ROLE_USER")));

        assertEquals("rick@example.com", jwt.extractUsername(token));
        assertTrue(jwt.isTokenValid(token, "rick@example.com"));
        assertFalse(jwt.isTokenValid(token, "someone@example.com"));
    }

    @Test
    void aTokenSignedWithAnotherKeyIsRejected() {
        String forged = Jwts.builder()
                            .setSubject("rick@example.com")
                            .setExpiration(new Date(System.currentTimeMillis() + 60_000))
                            .signWith(SignatureAlgorithm.HS256, "c29tZS1vdGhlci1zZWNyZXQtc29tZS1vdGhlci1zZWNyZXQtMTIzNDU2Nzg5MA==")
                            .compact();
        assertThrows(SignatureException.class, () -> jwt.extractUsername(forged));
    }

    @Test
    void aTamperedTokenIsRejected() {
        String token = jwt.generateToken("rick@example.com", Map.of());
        String[] parts = token.split("\\.");
        String tampered = parts[0] + "." + parts[1] + "x." + parts[2];
        assertThrows(Exception.class, () -> jwt.extractUsername(tampered));
    }

    @Test
    void anExpiredTokenIsRejected() throws Exception {
        String expired = Jwts.builder()
                             .setSubject("rick@example.com")
                             .setIssuedAt(new Date(System.currentTimeMillis() - 120_000))
                             .setExpiration(new Date(System.currentTimeMillis() - 60_000))
                             .signWith(SignatureAlgorithm.HS256, SECRET)
                             .compact();
        assertThrows(ExpiredJwtException.class, () -> jwt.isTokenValid(expired, "rick@example.com"));
    }

    @Test
    void theLifetimeComesFromConfiguration() {
        long before = System.currentTimeMillis();
        String token = service(900_000).generateToken("rick@example.com", Map.of());
        Date expires = Jwts.parserBuilder().setSigningKey(JwtServiceImpl.signingKey(SECRET)).build()
                           .parseClaimsJws(token).getBody().getExpiration();
        long life = expires.getTime() - before;
        assertTrue(life > 890_000 && life <= 901_000, "lifetime was " + life);
    }

    @Test
    void anUnsetLifetimeFallsBackToFifteenMinutes() {
        long before = System.currentTimeMillis();
        String token = service(0).generateToken("rick@example.com", Map.of());
        Date expires = Jwts.parserBuilder().setSigningKey(JwtServiceImpl.signingKey(SECRET)).build()
                           .parseClaimsJws(token).getBody().getExpiration();
        assertTrue(expires.getTime() - before > 14 * 60_000);
    }

    @Test
    void tokensFromAnotherSecretAreRejected() {
        JwtServiceImpl other = new JwtServiceImpl(properties(JwtServiceImpl.DEV_SECRET, 60_000));
        String token = other.generateToken("rick@example.com", Map.of());
        assertThrows(SignatureException.class, () -> jwt.extractUsername(token));
    }

    @Test
    void aMissingOrWeakSecretStopsStartup() {
        assertThrows(IllegalStateException.class, () -> new JwtServiceImpl(properties(null, 60_000)));
        assertThrows(IllegalStateException.class, () -> new JwtServiceImpl(properties("  ", 60_000)));
        assertThrows(IllegalStateException.class, () -> new JwtServiceImpl(properties("c2hvcnQ=", 60_000)));
        assertThrows(IllegalStateException.class, () -> new JwtServiceImpl(properties("too short!", 60_000)));
    }

    @Test
    void aLongPlainTextSecretIsAcceptedAsUtf8() {
        JwtServiceImpl plain = new JwtServiceImpl(properties("this is a long plain-text secret, well over 32 bytes!", 60_000));
        String token = plain.generateToken("rick@example.com", Map.of());
        assertEquals("rick@example.com", plain.extractUsername(token));
    }

    @Test
    void theShippedDevDefaultIsAValidKey() {
        assertTrue(JwtServiceImpl.signingKey(JwtServiceImpl.DEV_SECRET).getEncoded().length >= 32);
    }
}
