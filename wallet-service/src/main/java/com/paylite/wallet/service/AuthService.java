package com.paylite.wallet.service;

import com.paylite.wallet.config.JwtProperties;
import com.paylite.wallet.dto.AuthResponse;
import com.paylite.wallet.dto.LoginRequest;
import com.paylite.wallet.dto.UserResponse;
import com.paylite.wallet.entity.User;
import com.paylite.wallet.exception.InvalidCredentialsException;
import com.paylite.wallet.repository.UserRepository;
import com.paylite.wallet.security.JwtService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * Handles login — verifies credentials and issues a JWT.
 *
 * The actual password verification is delegated to Spring's AuthenticationManager,
 * which:
 *   1. calls CustomUserDetailsService.loadUserByUsername(email) → gets BCrypt hash
 *   2. calls BCryptPasswordEncoder.matches(plainPassword, hashFromDB)
 *   3. returns Authentication on success, throws on failure
 *
 * We only handle:
 *   - converting framework exceptions into our own InvalidCredentialsException
 *   - issuing the JWT
 *   - building the AuthResponse
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;
    private final UserRepository userRepository;

    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt for email={}", request.getEmail());

        // 1. Verify email + password via Spring's machinery
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );
        } catch (AuthenticationException ex) {
            // Catches: BadCredentialsException, UsernameNotFoundException, DisabledException, etc.
            // We treat all auth failures uniformly to avoid leaking which case it was.
            log.warn("Login failed for email={}: {}", request.getEmail(), ex.getClass().getSimpleName());
            throw new InvalidCredentialsException();
        }

        // 2. Credentials are valid. Load the User entity for the response.
        // Safe to assume the user exists at this point — AuthenticationManager just confirmed it.
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalStateException(
                        "User authenticated but not found in DB — race condition?"));

        // 3. Generate the JWT
        String token = jwtService.generateToken(user.getEmail());
        Instant expiresAt = Instant.now().plusMillis(jwtProperties.getExpirationMs());

        log.info("Login successful for email={}", user.getEmail());

        // 4. Build and return the response
        return AuthResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresAt(expiresAt)
                .user(UserResponse.from(user))
                .build();
    }
}