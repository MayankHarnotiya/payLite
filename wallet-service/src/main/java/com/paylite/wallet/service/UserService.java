package com.paylite.wallet.service;

import com.paylite.wallet.dto.SignupRequest;
import com.paylite.wallet.dto.UserResponse;
import com.paylite.wallet.entity.User;
import com.paylite.wallet.exception.EmailAlreadyExistsException;
import com.paylite.wallet.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Business logic for user-related operations: signup, lookup, etc.
 *
 * This class knows nothing about HTTP. It receives DTOs, returns DTOs,
 * uses the repository for persistence, and the password encoder for hashing.
 * The same code could be invoked from a REST controller, a Kafka consumer,
 * a scheduled job, or a test — all without modification.
 */
@Service                                // Marks this as a Spring-managed business component
@RequiredArgsConstructor                // Lombok: generates constructor for all `final` fields → enables constructor injection
@Slf4j                                  // Lombok: gives us a `log` field for SLF4J logging
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Sign up a new user.
     *
     * @param request validated input from the signup endpoint
     * @return UserResponse with generated id and timestamps; never includes password
     * @throws EmailAlreadyExistsException if the email is already registered
     */
    @Transactional
    public UserResponse signup(SignupRequest request) {
        log.info("Signup attempt for email={}", request.getEmail());

        // 1. Business rule: email must be unique
        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Signup rejected — email already exists: {}", request.getEmail());
            throw new EmailAlreadyExistsException(request.getEmail());
        }

        // 2. Hash the password BEFORE building the entity.
        //    Plain password never reaches the DB.
        String hashedPassword = passwordEncoder.encode(request.getPassword());

        // 3. Build the entity from the DTO. Notice id and timestamps are NOT set —
        //    @GeneratedValue picks the id, @CreationTimestamp picks createdAt.
        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(hashedPassword)
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .build();

        // 4. Persist. Returns the saved entity with the generated id and createdAt populated.
        User savedUser = userRepository.save(user);

        log.info("User signed up successfully: id={} email={}", savedUser.getId(), savedUser.getEmail());

        // 5. Convert entity to response DTO. The hash is dropped here at the boundary.
        return UserResponse.from(savedUser);
    }
}