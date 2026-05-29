package com.paylite.wallet.service;

import com.paylite.wallet.dto.TransferRequest;
import com.paylite.wallet.dto.TransferResponse;
import com.paylite.wallet.entity.Transaction;
import com.paylite.wallet.entity.User;
import com.paylite.wallet.entity.Wallet;
import com.paylite.wallet.exception.InsufficientBalanceException;
import com.paylite.wallet.exception.RecipientNotFoundException;
import com.paylite.wallet.exception.SelfTransferException;
import com.paylite.wallet.exception.WalletNotFoundException;
import com.paylite.wallet.repository.TransactionRepository;
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
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import com.paylite.wallet.messaging.TransferEventPublisher;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for TransferService.
 *
 * Tricky parts of testing this class:
 *   1. IdempotencyService.executeIdempotent takes a Supplier — we mock it to
 *      simply INVOKE the supplier (acting as if the request is the first, not a retry).
 *   2. TransactionTemplate.execute takes a callback — we mock it the same way
 *      (acting as if a transaction is started and the callback runs).
 *
 * This lets us test the actual transfer logic without touching Redis or the DB.
 */
@ExtendWith(MockitoExtension.class)
class TransferServiceTest {

    @Mock private WalletRepository walletRepository;
    @Mock private UserRepository userRepository;
    @Mock private TransactionRepository transactionRepository;
    @Mock private IdempotencyService idempotencyService;
    @Mock private TransactionTemplate transactionTemplate;
    @Mock private ObjectProvider<TransferEventPublisher> transferEventPublisher;

    @InjectMocks
    private TransferService transferService;

    private User sender;
    private User recipient;
    private Wallet senderWallet;
    private Wallet recipientWallet;
    private TransferRequest request;

    private static final String IDEM_KEY = "test-idempotency-key-12345";

    @BeforeEach
    void setUp() {
        sender = User.builder()
                .id(1L)
                .email("sender@paylite.com")
                .fullName("Sender User")
                .build();

        recipient = User.builder()
                .id(2L)
                .email("recipient@paylite.com")
                .fullName("Recipient User")
                .build();

        senderWallet = Wallet.builder()
                .id(10L)
                .user(sender)
                .balance(new BigDecimal("500.00"))
                .version(0L)
                .build();

        recipientWallet = Wallet.builder()
                .id(20L)
                .user(recipient)
                .balance(new BigDecimal("100.00"))
                .version(0L)
                .build();

        request = TransferRequest.builder()
                .recipientEmail("recipient@paylite.com")
                .amount(new BigDecimal("100.00"))
                .build();
    }

    /**
     * Helper: configures the mocks for IdempotencyService and TransactionTemplate
     * so that they just invoke the underlying business logic. This simulates a
     * "first request, no retry" scenario for every test.
     */
    private void wireFirstRequestSimulation() {
        // IdempotencyService.executeIdempotent: invoke the supplier and return its result
        when(idempotencyService.executeIdempotent(any(), any(), any()))
                .thenAnswer(invocation -> {
                    Supplier<?> supplier = invocation.getArgument(2);
                    return supplier.get();
                });

        // TransactionTemplate.execute: invoke the callback and return its result
        when(transactionTemplate.execute(any()))
                .thenAnswer(invocation -> {
                    TransactionCallback<?> callback = invocation.getArgument(0);
                    return callback.doInTransaction(null);
                });
    }

    @Test
    @DisplayName("transfer happy path: sender debited, recipient credited, transaction saved")
    void shouldDebitSenderAndCreditRecipientOnHappyPath() {
        // ARRANGE
        wireFirstRequestSimulation();
        when(walletRepository.findByUserEmail("sender@paylite.com"))
                .thenReturn(Optional.of(senderWallet));
        when(userRepository.findByEmail("recipient@paylite.com"))
                .thenReturn(Optional.of(recipient));
        when(walletRepository.findByUser(recipient))
                .thenReturn(Optional.of(recipientWallet));
        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(inv -> {
                    Transaction t = inv.getArgument(0);
                    t.setId(99L);  // simulate DB-assigned ID
                    return t;
                });

        // ACT
        TransferResponse response = transferService.transfer(
                IDEM_KEY, "sender@paylite.com", request);

        // ASSERT — response shape
        assertThat(response.getTransactionId()).isEqualTo(99L);
        assertThat(response.getSenderEmail()).isEqualTo("sender@paylite.com");
        assertThat(response.getRecipientEmail()).isEqualTo("recipient@paylite.com");
        assertThat(response.getAmount()).isEqualByComparingTo("100.00");
        assertThat(response.getStatus()).isEqualTo("COMPLETED");
        assertThat(response.getNewSenderBalance()).isEqualByComparingTo("400.00");
        assertThat(response.getCurrency()).isEqualTo("INR");

        // ASSERT — balances were mutated correctly (entity dirty-checking)
        assertThat(senderWallet.getBalance()).isEqualByComparingTo("400.00");
        assertThat(recipientWallet.getBalance()).isEqualByComparingTo("200.00");

        // ASSERT — transaction was persisted with the correct fields
        ArgumentCaptor<Transaction> txnCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(txnCaptor.capture());
        Transaction savedTxn = txnCaptor.getValue();
        assertThat(savedTxn.getSender()).isEqualTo(sender);
        assertThat(savedTxn.getRecipient()).isEqualTo(recipient);
        assertThat(savedTxn.getAmount()).isEqualByComparingTo("100.00");
        assertThat(savedTxn.getStatus()).isEqualTo("COMPLETED");
        assertThat(savedTxn.getIdempotencyKey()).isEqualTo(IDEM_KEY);
        assertThat(savedTxn.getCurrency()).isEqualTo("INR");
    }

    @Test
    @DisplayName("transfer preserves BigDecimal precision for sub-rupee amounts")
    void shouldPreserveBigDecimalPrecision() {
        // ARRANGE — transfer 33.33, then balance should be exact (500 - 33.33 = 466.67)
        wireFirstRequestSimulation();
        when(walletRepository.findByUserEmail("sender@paylite.com"))
                .thenReturn(Optional.of(senderWallet));
        when(userRepository.findByEmail("recipient@paylite.com"))
                .thenReturn(Optional.of(recipient));
        when(walletRepository.findByUser(recipient))
                .thenReturn(Optional.of(recipientWallet));
        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        request.setAmount(new BigDecimal("33.33"));

        // ACT
        TransferResponse response = transferService.transfer(
                IDEM_KEY, "sender@paylite.com", request);

        // ASSERT — exact arithmetic, no floating-point errors
        assertThat(response.getNewSenderBalance()).isEqualByComparingTo("466.67");
        assertThat(senderWallet.getBalance()).isEqualByComparingTo("466.67");
        assertThat(recipientWallet.getBalance()).isEqualByComparingTo("133.33");
    }

    @Test
    @DisplayName("transfer throws WalletNotFoundException when sender wallet is missing")
    void shouldThrowWhenSenderWalletNotFound() {
        // ARRANGE
        wireFirstRequestSimulation();
        when(walletRepository.findByUserEmail("sender@paylite.com"))
                .thenReturn(Optional.empty());

        // ACT + ASSERT
        assertThatThrownBy(() -> transferService.transfer(
                IDEM_KEY, "sender@paylite.com", request))
                .isInstanceOf(WalletNotFoundException.class);

        // CRITICAL: no transaction was saved
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    @DisplayName("transfer throws RecipientNotFoundException when recipient email is unknown")
    void shouldThrowWhenRecipientNotFound() {
        // ARRANGE
        wireFirstRequestSimulation();
        when(walletRepository.findByUserEmail("sender@paylite.com"))
                .thenReturn(Optional.of(senderWallet));
        when(userRepository.findByEmail("recipient@paylite.com"))
                .thenReturn(Optional.empty());

        // ACT + ASSERT
        assertThatThrownBy(() -> transferService.transfer(
                IDEM_KEY, "sender@paylite.com", request))
                .isInstanceOf(RecipientNotFoundException.class)
                .hasMessageContaining("recipient@paylite.com");

        verify(transactionRepository, never()).save(any(Transaction.class));
        // Sender's wallet should not be mutated
        assertThat(senderWallet.getBalance()).isEqualByComparingTo("500.00");
    }

    @Test
    @DisplayName("transfer throws SelfTransferException when sender == recipient")
    void shouldThrowWhenSenderEqualsRecipient() {
        // ARRANGE — same wallet returned for both sender and recipient lookup
        wireFirstRequestSimulation();
        when(walletRepository.findByUserEmail("sender@paylite.com"))
                .thenReturn(Optional.of(senderWallet));
        when(userRepository.findByEmail(any())).thenReturn(Optional.of(sender));
        when(walletRepository.findByUser(sender)).thenReturn(Optional.of(senderWallet));

        request.setRecipientEmail("sender@paylite.com");  // self-transfer

        // ACT + ASSERT
        assertThatThrownBy(() -> transferService.transfer(
                IDEM_KEY, "sender@paylite.com", request))
                .isInstanceOf(SelfTransferException.class)
                .hasMessageContaining("Cannot transfer money to yourself");

        verify(transactionRepository, never()).save(any(Transaction.class));
        assertThat(senderWallet.getBalance()).isEqualByComparingTo("500.00");
    }

    @Test
    @DisplayName("transfer throws InsufficientBalanceException when balance < amount")
    void shouldThrowWhenBalanceInsufficient() {
        // ARRANGE — sender has 500, request 10,000
        wireFirstRequestSimulation();
        when(walletRepository.findByUserEmail("sender@paylite.com"))
                .thenReturn(Optional.of(senderWallet));
        when(userRepository.findByEmail("recipient@paylite.com"))
                .thenReturn(Optional.of(recipient));
        when(walletRepository.findByUser(recipient))
                .thenReturn(Optional.of(recipientWallet));

        request.setAmount(new BigDecimal("10000.00"));

        // ACT + ASSERT
        assertThatThrownBy(() -> transferService.transfer(
                IDEM_KEY, "sender@paylite.com", request))
                .isInstanceOf(InsufficientBalanceException.class)
                .hasMessageContaining("500.00")    // available
                .hasMessageContaining("10000.00"); // requested

        verify(transactionRepository, never()).save(any(Transaction.class));
        // No mutations: both wallets unchanged
        assertThat(senderWallet.getBalance()).isEqualByComparingTo("500.00");
        assertThat(recipientWallet.getBalance()).isEqualByComparingTo("100.00");
    }
}