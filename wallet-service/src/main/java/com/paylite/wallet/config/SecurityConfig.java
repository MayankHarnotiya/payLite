package com.paylite.wallet.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security configuration.
 *
 * For now (Step 3), we permit ALL requests so we can build and test the
 * signup endpoint without auth getting in the way.
 *
 * In a later step, we will lock down most endpoints and add JWT-based
 * authentication for protected ones.
 */
@Configuration
public class SecurityConfig {

    /**
     * The HTTP security rules.
     * Spring Security's default behavior locks every endpoint behind a login.
     * We override that here to allow all requests during development.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF — it's a browser-form protection that gets in the way of REST APIs
                .csrf(csrf -> csrf.disable())
                // Permit every request. We'll tighten this in a later step.
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()
                );
        return http.build();
    }

    /**
     * The BCrypt password encoder bean.
     * Used by UserService to hash passwords before saving and to verify them on login.
     * Spring's @Autowired will inject this wherever a PasswordEncoder is needed.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}