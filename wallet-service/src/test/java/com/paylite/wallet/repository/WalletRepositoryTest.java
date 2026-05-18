package com.paylite.wallet.repository;

import com.paylite.wallet.AbstractIntegrationTest;
import com.paylite.wallet.entity.User;
import com.paylite.wallet.entity.Wallet;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Slice test for WalletRepository — exercises the actual JPQL/SQL queries
 * against a real MySQL container (via Testcontainers).
 *
 * Why slice tests: mocks don't validate that Spring Data JPA generated the
 * right query. Here we INSERT a user + wallet, then call our derived
 * `findByUserEmail` to verify the auto-generated JOIN actually returns the row.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class WalletRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("findByUserEmail returns the wallet when user exists")
    void shouldFindWalletByUserEmail() {
        // ARRANGE — persist a user and a wallet for them
        User user = userRepository.save(User.builder()
                .email("repo-test@paylite.com")
                .passwordHash("$2a$10$dummy")
                .fullName("Repo Test")
                .phone("9876543210")
                .build());

        walletRepository.save(Wallet.builder()
                .user(user)
                .balance(new BigDecimal("0.00"))
                .build());

        // ACT
        Optional<Wallet> found = walletRepository.findByUserEmail("repo-test@paylite.com");

        // ASSERT — the JOIN query worked, the right wallet came back
        assertThat(found).isPresent();
        assertThat(found.get().getUser().getEmail()).isEqualTo("repo-test@paylite.com");
        assertThat(found.get().getBalance()).isEqualByComparingTo("0.00");
    }

    @Test
    @DisplayName("findByUserEmail returns empty for non-existent email")
    void shouldReturnEmptyForUnknownEmail() {
        Optional<Wallet> found = walletRepository.findByUserEmail("ghost@paylite.com");
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("DB enforces CHECK constraint: balance cannot go negative")
    void dbRejectsNegativeBalance() {
        // ARRANGE
        User user = userRepository.save(User.builder()
                .email("check-test@paylite.com")
                .passwordHash("$2a$10$dummy")
                .fullName("Check Test")
                .phone("9876543210")
                .build());

        Wallet wallet = walletRepository.save(Wallet.builder()
                .user(user)
                .balance(new BigDecimal("100.00"))
                .build());

        // ACT — try to set balance to -50 and flush to DB
        wallet.setBalance(new BigDecimal("-50.00"));

        // ASSERT — MySQL's CHECK constraint rejects the UPDATE
        org.junit.jupiter.api.Assertions.assertThrows(Exception.class, () -> {
            walletRepository.saveAndFlush(wallet);
        }, "DB should reject negative balances via CHECK constraint");
    }
}