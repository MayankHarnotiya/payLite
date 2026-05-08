package com.paylite.wallet.security;

import com.paylite.wallet.config.JwtProperties;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

/**
 * Generates and parses JWT tokens.
 *
 * Token shape (HS256):
 *   header.payload.signature
 *
 * Payload claims:
 *   - sub (subject): user's email
 *   - iss (issuer):  "paylite-wallet-service"
 *   - iat (issued):  current timestamp
 *   - exp (expires): iat + 15 minutes
 *
 * The secret key is decoded from Base64 once at construction time, then
 * cached. This service is stateless — safe to inject everywhere.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class JwtService {

    private final JwtProperties jwtProperties;

    /**
     * Decode the Base64 secret into bytes and turn into an HMAC SecretKey.
     * Computed every call here for simplicity — fine performance-wise since
     * it's just memory operations.
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtProperties.getSecret());
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Generate a JWT for the given user email.
     * Sets sub, iss, iat, exp; signs with HS256.
     */
    public String generateToken(String email) {
        Instant now = Instant.now();
        Instant expiry = now.plusMillis(jwtProperties.getExpirationMs());

        String token = Jwts.builder()
                .subject(email)                     // sub: who this token is for
                .issuer(jwtProperties.getIssuer())  // iss: who created it
                .issuedAt(Date.from(now))           // iat: when issued
                .expiration(Date.from(expiry))      // exp: when it stops working
                .signWith(getSigningKey())          // sign with our secret (defaults to HS256 for HMAC keys)
                .compact();                         // serialize to header.payload.signature

        log.debug("Generated JWT for email={} expiringAt={}", email, expiry);
        return token;
    }

    /**
     * Extract the email (subject) from a token, validating signature + expiry.
     * Returns Optional.empty() on any validation failure (bad signature,
     * expired, malformed) — caller decides how to react.
     */
    public Optional<String> extractEmail(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())     // verify signature with our secret
                    .build()
                    .parseSignedClaims(token)        // throws if signature/expiry wrong
                    .getPayload();

            return Optional.ofNullable(claims.getSubject());

        } catch (JwtException ex) {
            // Covers: ExpiredJwtException, MalformedJwtException, SignatureException, etc.
            log.debug("JWT validation failed: {}", ex.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Convenience: just check if the token is valid (signature + not expired)
     * without extracting anything. Used by the filter for early rejection.
     */
    public boolean isValid(String token) {
        return extractEmail(token).isPresent();
    }
}