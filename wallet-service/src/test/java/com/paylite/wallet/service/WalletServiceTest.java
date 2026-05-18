package com.paylite.wallet.service;

import com.paylite.wallet.dto.AddMoneyRequest;
import com.paylite.wallet.dto.WalletResponse;
import com.paylite.wallet.entity.User;
import com.paylite.wallet.entity.Wallet;
import com.paylite.wallet.exception.WalletNotFoundException;
import com.paylite.wallet.repository.WalletRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * Unit tests for WalletService.
 *
 * Tests:
 *   - getWalletByEmail happy path + missing-wallet case
 *   - addMoney happy path with BigDecimal arithmetic (precision verification)
 *   - addMoney correctly updates balance for accumulating credits
 *   - addMoney throws WalletNotFoundException for missing wallet
 *
 * No Spring context. WalletRepository is mocked.
 */
@ExtendWith(MockitoExtension.class)
class WalletServiceTest {

    @Mock
    private WalletRepository walletRepository;

    @InjectMocks
    private WalletService walletService;

    private User testUser;
    private Wallet testWallet;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("user@paylite.com")
                .passwordHash("$2a$10$dummyHash")
                .fullName("Test User")
                .phone("9876543210")
                .build();

        testWallet = Wallet.builder()
                .id(10L)
                .user(testUser)
                .balance(new BigDecimal("100.00"))
                .version(0L)
                .createdAt(LocalDateTime.now().minusDays(1))
                .updatedAt(LocalDateTime.now().minusDays(1))
                .build();
    }

    @Test
    @DisplayName("getWalletByEmail returns balance, currency, and lastUpdated")
    void shouldReturnWalletDetailsForExistingUser() {
        // ARRANGE
        when(walletRepository.findByUserEmail("user@paylite.com"))
                .thenReturn(Optional.of(testWallet));

        // ACT
        WalletResponse response = walletService.getWalletByEmail("user@paylite.com");

        // ASSERT
        assertThat(response).isNotNull();
        assertThat(response.getBalance()).isEqualByComparingTo("100.00");
        assertThat(response.getCurrency()).isEqualTo("INR");
        assertThat(response.getLastUpdated()).isEqualTo(testWallet.getUpdatedAt());
    }

    @Test
    @DisplayName("getWalletByEmail throws WalletNotFoundException when wallet is missing")
    void shouldThrowWhenWalletNotFound() {
        // ARRANGE
        when(walletRepository.findByUserEmail("ghost@paylite.com"))
                .thenReturn(Optional.empty());

        // ACT + ASSERT
        assertThatThrownBy(() -> walletService.getWalletByEmail("ghost@paylite.com"))
                .isInstanceOf(WalletNotFoundException.class)
                .hasMessageContaining("ghost@paylite.com");
    }

    @Test
    @DisplayName("addMoney correctly credits the wallet with BigDecimal precision")
    void shouldCreditWalletPreservingDecimalPrecision() {
        // ARRANGE
        when(walletRepository.findByUserEmail("user@paylite.com"))
                .thenReturn(Optional.of(testWallet));
        AddMoneyRequest request = AddMoneyRequest.builder()
                .amount(new BigDecimal("250.50"))
                .build();

        // ACT
        WalletResponse response = walletService.addMoney("user@paylite.com", request);

        // ASSERT — 100.00 + 250.50 = 350.50 (exact, no float errors)
        assertThat(response.getBalance()).isEqualByComparingTo("350.50");
        assertThat(testWallet.getBalance()).isEqualByComparingTo("350.50");  // entity mutated
    }

    @Test
    @DisplayName("addMoney accumulates correctly across two credits")
    void shouldAccumulateMultipleCredits() {
        // ARRANGE
        when(walletRepository.findByUserEmail("user@paylite.com"))
                .thenReturn(Optional.of(testWallet));

        // ACT — credit 50.25 then 49.75. Total: 200.00
        walletService.addMoney("user@paylite.com",
                AddMoneyRequest.builder().amount(new BigDecimal("50.25")).build());
        WalletResponse finalResponse = walletService.addMoney("user@paylite.com",
                AddMoneyRequest.builder().amount(new BigDecimal("49.75")).build());

        // ASSERT — 100.00 + 50.25 + 49.75 = 200.00
        assertThat(finalResponse.getBalance()).isEqualByComparingTo("200.00");
    }

    @Test
    @DisplayName("addMoney throws WalletNotFoundException for missing wallet")
    void shouldThrowWhenAddingToMissingWallet() {
        // ARRANGE
        when(walletRepository.findByUserEmail("ghost@paylite.com"))
                .thenReturn(Optional.empty());
        AddMoneyRequest request = AddMoneyRequest.builder()
                .amount(new BigDecimal("100.00"))
                .build();

        // ACT + ASSERT
        assertThatThrownBy(() -> walletService.addMoney("ghost@paylite.com", request))
                .isInstanceOf(WalletNotFoundException.class)
                .hasMessageContaining("ghost@paylite.com");
    }
}