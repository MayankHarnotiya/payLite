package com.paylite.wallet.config;

import lombok.Getter;
import lombok.Setter;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Type-safe binding of paylite.jwt.* properties from application.yml.
 *
 * Spring scans for @ConfigurationProperties and auto-populates the fields
 * from matching YAML keys. Usage in any service:
 *   private final JwtProperties jwtProperties;
 *   String secret = jwtProperties.getSecret();
 *
 * Cleaner than scattering @Value("${paylite.jwt.secret}") strings everywhere.
 */
@Component
@ConfigurationProperties(prefix = "paylite.jwt")
@Getter
@Setter
public class JwtProperties {

    /** Base64-encoded HMAC-SHA256 secret. Must be ≥256 bits (32 bytes). */
    private String secret;

    /** Access token validity in milliseconds. */
    private long expirationMs;

    /** Issuer claim — the service that issued the token. */
    private String issuer;
}