package com.paylite.wallet.security;

import com.paylite.wallet.entity.User;
import com.paylite.wallet.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * Spring Security's bridge to OUR user table.
 *
 * Spring Security calls loadUserByUsername(email) whenever it needs to verify
 * credentials. We look up the user in MySQL by email and return a UserDetails
 * object that Spring can use to compare passwords.
 *
 * Note: Spring's interface uses "username" generically — for PayLite, that's email.
 * If the user doesn't exist, we throw UsernameNotFoundException — Spring catches
 * this and returns 401 Unauthorized to the client.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.debug("Looking up user by email: {}", email);

        // Find our app's User entity from MySQL
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("User not found: {}", email);
                    return new UsernameNotFoundException("User not found: " + email);
                });

        // Wrap in Spring's UserDetails (different class, same name as ours)
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())              // Spring's "username" = our email
                .password(user.getPasswordHash())       // BCrypt hash from DB
                .authorities(Collections.emptyList())   // No roles yet — add USER, ADMIN later
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(false)
                .build();
    }
}