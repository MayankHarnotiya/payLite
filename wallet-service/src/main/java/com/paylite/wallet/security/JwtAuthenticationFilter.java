package com.paylite.wallet.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Reads the Authorization: Bearer <token> header on every request.
 * If the token is valid, attaches the authenticated user to the SecurityContext
 * for the duration of the request.
 *
 * Extends OncePerRequestFilter so this runs exactly once per HTTP request,
 * even if the request gets internally forwarded by Spring.
 *
 * IMPORTANT: this filter does NOT decide which endpoints need auth.
 * It just *informs Spring* whether the requester has a valid token.
 * The actual authorization rules live in SecurityConfig (.authorizeHttpRequests).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain) throws ServletException, IOException {

        // 1. Try to extract the token from the Authorization header
        String token = extractToken(request);

        // 2. If no token OR token already authenticated, just pass through
        if (token == null || SecurityContextHolder.getContext().getAuthentication() != null) {
            chain.doFilter(request, response);
            return;
        }

        // 3. Validate the token and extract the email
        jwtService.extractEmail(token).ifPresent(email -> {
            try {
                // 4. Load the user details from DB
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                // 5. Build the authentication object Spring needs
                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,                              // credentials = null (we already verified via JWT)
                                userDetails.getAuthorities()       // roles/permissions
                        );

                // Attach extra metadata: remote IP, session ID, etc.
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // 6. Set in SecurityContext for the duration of this request
                SecurityContextHolder.getContext().setAuthentication(auth);
                log.debug("Authenticated user via JWT: {}", email);

            } catch (Exception ex) {
                log.warn("Failed to authenticate user from JWT: {}", ex.getMessage());
                // Fall through — request continues unauthenticated
            }
        });

        // 7. Always continue the chain — auth rules in SecurityConfig decide what happens next
        chain.doFilter(request, response);
    }

    /**
     * Extracts the raw token from "Authorization: Bearer <token>".
     * Returns null if header is missing or malformed.
     */
    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader(AUTH_HEADER);
        if (header != null && header.startsWith(BEARER_PREFIX)) {
            return header.substring(BEARER_PREFIX.length());
        }
        return null;
    }
}