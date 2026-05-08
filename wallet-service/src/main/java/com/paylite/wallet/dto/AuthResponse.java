package com.paylite.wallet.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * Output shape for POST /api/auth/login.
 *
 * Returns:
 *   - accessToken: the JWT to use in Authorization: Bearer ... headers
 *   - tokenType: "Bearer" — RFC 6750 standard, helps client know how to use it
 *   - expiresAt: when the token stops working (informational; client can pre-refresh)
 *   - user: profile info so the client doesn't need a separate "who am I" call
 *
 * No refresh token yet — we'll add that later. For now, when the access token
 * expires, the client must re-login.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {

    private String accessToken;
    private String tokenType;       // always "Bearer" for JWT
    private Instant expiresAt;
    private UserResponse user;
}