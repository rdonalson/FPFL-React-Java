package com.financialplanner.moduleauth.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AuthConfigTest {

    @Test
    void passwordsAreHashedWithBcrypt() {
        var encoder = new PasswordEncoderConfig().passwordEncoder();
        String hash = encoder.encode("Password@2");
        assertTrue(hash.startsWith("$2"));
        assertTrue(encoder.matches("Password@2", hash));
        assertFalse(encoder.matches("password@2", hash));
    }

    @Test
    void tokenPropertiesHoldTheJwtSettings() {
        TokenProperties p = new TokenProperties();
        p.setSecret("s");
        p.setAccessTokenExpirationMs(900_000);
        p.setRefreshTokenExpirationMs(1_800_000);
        assertEquals("s", p.getSecret());
        assertEquals(900_000, p.getAccessTokenExpirationMs());
        assertEquals(1_800_000, p.getRefreshTokenExpirationMs());
    }
}
