package com.paylite.wallet.config;

import com.paylite.wallet.security.JwtAuthenticationFilter;

import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security configuration for PayLite.
 *
 * Strategy:
 *   - Stateless (no HTTP sessions) — REST APIs use JWT instead.
 *   - Public endpoints: /api/auth/** and /actuator/**.
 *   - Every other endpoint requires authentication.
 *   - Our JwtAuthenticationFilter runs BEFORE Spring's
 *     UsernamePasswordAuthenticationFilter to populate SecurityContext from JWT.
 */
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // CSRF is for browser sessions with cookies. Stateless REST APIs don't need it.
                .csrf(AbstractHttpConfigurer::disable)

                // No HTTP sessions. Each request must carry its own credentials (JWT).
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // URL rules — order matters: most specific first.
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/actuator/health").permitAll()
                        .requestMatchers("/actuator/**").permitAll()
                        .anyRequest().authenticated()
                )

                // Insert our JWT filter BEFORE Spring's username/password filter,
                // so SecurityContext is populated from JWT before any other auth check.
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Expose AuthenticationManager as a bean.
     * Required by AuthService at login time to verify email + password.
     * Spring builds this internally based on our CustomUserDetailsService + PasswordEncoder.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}