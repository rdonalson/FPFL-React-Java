package com.financialplanner.moduleauth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@lombok.Data
@Component
@ConfigurationProperties(prefix = "security.jwt")
public class TokenProperties {
    private String secret;
    private long accessTokenExpirationMs;
    private long refreshTokenExpirationMs;
}
