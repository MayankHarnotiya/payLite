package com.paylite.wallet.service;

import com.paylite.wallet.dto.SignupRequest;
import com.paylite.wallet.dto.UserResponse;
import com.paylite.wallet.entity.User;
import com.paylite.wallet.entity.Wallet;
import com.paylite.wallet.exception.EmailAlreadyExistsException;
import com.paylite.wallet.repository.UserRepository;
import com.paylite.wallet.repository.WalletRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for UserService.
 *
 * Style: pure unit tests. No Spring context, no real DB, no real BCrypt.
 * All dependencies (UserRepository, WalletRepository, PasswordEncoder) are mocked
 * so we test only the behavior of UserService in isolation.
 *
 * Pattern: Arrange-Act-Assert (AAA) — set up mocks, call the method, verify outcomes.
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private SignupRequest validRequest;

    @BeforeEach
    void setUp() {
        // A reusable valid signup request for happy-path tests
        validRequest = SignupRequest.builder()
                .email("test@paylite.com")
                .password("secret123")
                .fullName("Test User")
                .phone("9876543210")
                .build();
    }

    @Test
    @DisplayName("signup() creates user and wallet atomically on happy path")
    void shouldCreateUserAndWalletOnSuccessfulSignup() {
        // ARRANGE
        when(userRepository.existsByEmail(validRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(validRequest.getPassword())).thenReturn("hashed-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(42L); // simulate DB-assigned ID
            return u;
        });

        // ACT
        UserResponse response = userService.signup(validRequest);

        // ASSERT — response shape
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(42L);
        assertThat(response.getEmail()).isEqualTo("test@paylite.com");
        assertThat(response.getFullName()).isEqualTo("Test User");

        // ASSERT — password was hashed (not stored as plaintext)
        verify(passwordEncoder, times(1)).encode("secret123");

        // ASSERT — user was saved
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getPasswordHash()).isEqualTo("hashed-password");
        assertThat(savedUser.getPasswordHash()).isNotEqualTo("secret123");  // critical security check

        // ASSERT — wallet was created with zero balance for this user
        ArgumentCaptor<Wallet> walletCaptor = ArgumentCaptor.forClass(Wallet.class);
        verify(walletRepository).save(walletCaptor.capture());
        Wallet savedWallet = walletCaptor.getValue();
        assertThat(savedWallet.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(savedWallet.getUser()).isEqualTo(savedUser);
    }

    @Test
    @DisplayName("signup() throws EmailAlreadyExistsException when email is taken")
    void shouldRejectDuplicateEmail() {
        // ARRANGE
        when(userRepository.existsByEmail(validRequest.getEmail())).thenReturn(true);

        // ACT + ASSERT
        assertThatThrownBy(() -> userService.signup(validRequest))
                .isInstanceOf(EmailAlreadyExistsException.class)
                .hasMessageContaining(validRequest.getEmail());

        // CRITICAL: when email is duplicate, NO save calls should happen
        verify(userRepository, never()).save(any(User.class));
        verify(walletRepository, never()).save(any(Wallet.class));
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    @DisplayName("signup() always hashes the password before storing")
    void shouldNeverStorePlaintextPassword() {
        // ARRANGE
        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(passwordEncoder.encode(eq("secret123"))).thenReturn("$2a$10$abcdefghijklmnop...");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        // ACT
        userService.signup(validRequest);

        // ASSERT — capture the user that was passed to save() and verify its password field
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());

        String storedHash = captor.getValue().getPasswordHash();
        assertThat(storedHash)
                .as("Password must be hashed, never plaintext")
                .isNotEqualTo("secret123")
                .startsWith("$2a$10$");  // BCrypt-style hash prefix
    }
}