package com.paylite.wallet.controller;

import com.paylite.wallet.dto.AuthResponse;
import com.paylite.wallet.dto.LoginRequest;
import com.paylite.wallet.dto.SignupRequest;
import com.paylite.wallet.dto.UserResponse;
import com.paylite.wallet.service.AuthService;
import com.paylite.wallet.service.UserService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * HTTP entry point for authentication-related endpoints.
 *
 * Endpoints:
 *   POST /api/auth/signup → create a new user (returns 201 Created)
 *   POST /api/auth/login  → verify credentials, return JWT (returns 200 OK)
 *
 * All paths are public (configured in SecurityConfig). The JWT filter still
 * runs but tokens aren't required here — that's the whole point of these
 * endpoints: getting authenticated in the first place.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final UserService userService;
    private final AuthService authService;

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse signup(@Valid @RequestBody SignupRequest request) {
        log.debug("POST /api/auth/signup received for email={}", request.getEmail());
        return userService.signup(request);
    }

    /**
     * Verifies email + password, returns a JWT.
     * Returns 200 OK (not 201) — login doesn't create a new resource.
     *
     * Errors (handled by GlobalExceptionHandler):
     *   400 Bad Request    — missing/invalid fields
     *   401 Unauthorized   — wrong email or password
     *
     * Successful response:
     *   { accessToken, tokenType, expiresAt, user: { id, email, fullName, ... } }
     */
    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        log.debug("POST /api/auth/login received for email={}", request.getEmail());
        return authService.login(request);
    }
}