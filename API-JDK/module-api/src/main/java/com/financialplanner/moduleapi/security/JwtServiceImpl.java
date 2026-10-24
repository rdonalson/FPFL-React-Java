package com.financialplanner.moduleapi.security;

import com.financialplanner.moduleauth.config.TokenProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

/**
 * Issues and checks the access-token JWTs (HS256). The signing secret and the token lifetime come from
 * configuration ({@code security.jwt.secret}, {@code security.jwt.access-token-expiration-ms}); set the
 * {@code JWT_SECRET} environment variable in every deployed environment.
 */
@Service
public class JwtServiceImpl implements JwtService {

    private static final Logger LOG = LoggerFactory.getLogger(JwtServiceImpl.class);

    /** The local-development default in application.yaml; never use it in a deployed environment. */
    static final String DEV_SECRET = "ZnBmbC1sb2NhbC1kZXYtb25seS1qd3Qtc2VjcmV0LW5vdC1mb3ItcHJvZHVjdGlvbiEh";
    private static final long DEFAULT_EXPIRATION_MS = 15 * 60 * 1000L;

    private final SecretKey key;
    private final long expirationMs;

    public JwtServiceImpl(TokenProperties properties) {
        this.key = signingKey(properties.getSecret());
        this.expirationMs = properties.getAccessTokenExpirationMs() > 0 ? properties.getAccessTokenExpirationMs()
                                                                        : DEFAULT_EXPIRATION_MS;
        if (DEV_SECRET.equals(properties.getSecret())) {
            LOG.warn("security.jwt.secret is the local-development default; set JWT_SECRET for any deployed environment");
        }
    }

    /**
     * The HMAC key for a configured secret: Base64 is decoded, anything else is used as UTF-8 text. It must
     * give at least 256 bits, so a weak or missing secret stops the app at startup instead of signing tokens.
     */
    static SecretKey signingKey(String secret) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("security.jwt.secret (JWT_SECRET) is not set");
        }
        byte[] bytes;
        try {
            bytes = Decoders.BASE64.decode(secret.trim());
        } catch (RuntimeException notBase64) {
            bytes = secret.getBytes(StandardCharsets.UTF_8);
        }
        if (bytes.length < 32) {
            throw new IllegalStateException("security.jwt.secret (JWT_SECRET) must be at least 256 bits (32 bytes)");
        }
        return Keys.hmacShaKeyFor(bytes);
    }

    @Override
    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    @Override
    public String generateToken(String subject, Map<String, Object> claims) {
        return Jwts.builder()
                   .setClaims(claims)
                   .setSubject(subject)
                   .setIssuedAt(new Date())
                   .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
                   .signWith(key, SignatureAlgorithm.HS256)
                   .compact();
    }

    @Override
    public boolean isTokenValid(String token, String subject) {
        final String extractedSubject = extractUsername(token);
        return extractedSubject.equals(subject) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractAllClaims(token).getExpiration().before(new Date());
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                   .setSigningKey(key)
                   .build()
                   .parseClaimsJws(token)
                   .getBody();
    }
}
