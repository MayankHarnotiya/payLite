package com.paylite.wallet.controller;

import com.paylite.wallet.dto.SignupRequest;
import com.paylite.wallet.dto.UserResponse;
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
 * Right now: signup. We'll add /login in the next session (with JWT).
 *
 * Notice this class is short — it's only HTTP wiring. All business logic
 * lives in UserService. Same pattern in every Spring app: thin controller,
 * fat service.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final UserService userService;

    /**
     * Handles POST /api/auth/signup
     *
     * @param request validated signup payload (email, password, fullName, phone)
     * @return UserResponse with the new user's id and details
     *
     * Returns HTTP 201 Created on success (the standard status for resource creation).
     * If email is already taken, UserService throws EmailAlreadyExistsException —
     * we'll wire that to HTTP 409 Conflict in Step 10's exception handler.
     */
    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse signup(@Valid @RequestBody SignupRequest request) {
        log.debug("POST /api/auth/signup received for email={}", request.getEmail());
        return userService.signup(request);
    }
}