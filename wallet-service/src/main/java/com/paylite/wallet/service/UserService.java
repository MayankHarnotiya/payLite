package com.paylite.wallet.service;

import com.paylite.wallet.dto.SignupRequest;
import com.paylite.wallet.dto.UserResponse;
import com.paylite.wallet.entity.User;
import com.paylite.wallet.entity.Wallet;
import com.paylite.wallet.exception.EmailAlreadyExistsException;
import com.paylite.wallet.repository.UserRepository;
import com.paylite.wallet.repository.WalletRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Business logic for user-related operations: signup, lookup, etc.
 *
 * On signup: also auto-creates a Wallet for the new user, atomically.
 * @Transactional ensures both INSERTs commit together or both roll back —
 * we never have a user without a wallet.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserResponse signup(SignupRequest request) {
        log.info("Signup attempt for email={}", request.getEmail());

        // 1. Business rule: email must be unique
        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Signup rejected — email already exists: {}", request.getEmail());
            throw new EmailAlreadyExistsException(request.getEmail());
        }

        // 2. Hash the password before building the entity
        String hashedPassword = passwordEncoder.encode(request.getPassword());

        // 3. Build and save the user
        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(hashedPassword)
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .build();

        User savedUser = userRepository.save(user);

        // 4. Auto-create the user's wallet — starts at zero balance.
        //    If THIS fails, the user INSERT in step 3 rolls back too (same transaction).
        Wallet wallet = Wallet.builder()
                .user(savedUser)
                .balance(BigDecimal.ZERO)
                .build();

        walletRepository.save(wallet);

        log.info("User and wallet created: userId={} email={}", savedUser.getId(), savedUser.getEmail());

        // 5. Return response (UserResponse doesn't include wallet info; that's a separate endpoint)
        return UserResponse.from(savedUser);
    }
}