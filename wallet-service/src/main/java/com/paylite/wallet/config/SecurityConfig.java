package com.paylite.wallet.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security configuration for PayLite.
 *
 * Strategy:
 *   - Stateless (no HTTP sessions) — REST APIs use JWT instead.
 *   - Public endpoints listed explicitly: /api/auth/** and /actuator/health.
 *   - Every other endpoint requires authentication.
 *
 * In Day 5 we'll add a JWT filter that, on every request:
 *   1. Reads the Authorization: Bearer <token> header
 *   2. Validates the token's signature
 *   3. Extracts the user's identity and attaches it to the SecurityContext
 *
 * For now (no JWT yet) the protected endpoints aren't reachable —
 * but that's fine because we don't have any protected endpoints yet either.
 */
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // CSRF protection is for browser sessions with cookies.
                // We're stateless + token-based, so we disable it.
                .csrf(AbstractHttpConfigurer::disable)

                // Tell Spring Security: don't create HTTP sessions.
                // Each request must carry its own credentials (later: JWT).
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // The actual URL rules — order matters: most specific first.
                .authorizeHttpRequests(auth -> auth
                        // Auth endpoints (signup, login) — public. Anyone can hit them.
                        .requestMatchers("/api/auth/**").permitAll()

                        // Health checks — public so monitoring tools can poll without credentials.
                        .requestMatchers(HttpMethod.GET, "/actuator/health").permitAll()

                        // Other actuator endpoints (metrics, info) — admin only in real life.
                        // For now, permit them too. We'll lock down in production.
                        .requestMatchers("/actuator/**").permitAll()

                        // Everything else — must be authenticated.
                        .anyRequest().authenticated()
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}