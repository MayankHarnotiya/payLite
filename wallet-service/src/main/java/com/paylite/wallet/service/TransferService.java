package com.paylite.wallet.service;

import com.paylite.events.TransferCompletedEvent;
import com.paylite.wallet.dto.TransferRequest;
import com.paylite.wallet.dto.TransferResponse;
import com.paylite.wallet.messaging.TransferEventPublisher;
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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * Orchestrates the full money transfer flow with idempotency, atomicity,
 * and audit-trail recording.
 *
 * Architecture:
 *   - transfer() is the public entry point. NOT @Transactional — wraps in idempotency.
 *   - The actual DB work happens inside a TransactionTemplate callback, which
 *     starts a fresh @Transactional boundary at exactly the moment we need it.
 *
 * Why TransactionTemplate instead of @Transactional self-injection?
 *   - Spring Boot 3.x disallows self-injection cycles even with @Lazy
 *   - TransactionTemplate is explicit, no AOP magic
 *   - Same atomicity guarantees: callback commits or rolls back as one unit
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TransferService {

    private final WalletRepository walletRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final IdempotencyService idempotencyService;
    private final TransactionTemplate transactionTemplate;
    private final ObjectProvider<TransferEventPublisher> transferEventPublisher;

    /**
     * Public entry point — wraps the operation in idempotency logic.
     *
     * IMPORTANT: NOT @Transactional. The transactional boundary is opened inside
     * the supplier, only after IdempotencyService confirms we are the first request.
     */
    public TransferResponse transfer(
            String idempotencyKey,
            String senderEmail,
            TransferRequest request) {

        log.info("Transfer request: sender={} recipient={} amount={} idempotencyKey={}",
                senderEmail, request.getRecipientEmail(), request.getAmount(), idempotencyKey);

        return idempotencyService.executeIdempotent(
                idempotencyKey,
                TransferResponse.class,
                () -> {
                    TransferResponse response = transactionTemplate.execute(status ->
                            performTransfer(idempotencyKey, senderEmail, request));

                    // Publish only after DB commit (TransactionTemplate finished successfully).
                    // ObjectProvider is empty when paylite.kafka.enabled=false (tests).
                    transferEventPublisher.ifAvailable(publisher ->
                            publisher.publish(toEvent(response, idempotencyKey)));

                    return response;
                }
        );
    }

    private static TransferCompletedEvent toEvent(TransferResponse response, String idempotencyKey) {
        return TransferCompletedEvent.builder()
                .transactionId(response.getTransactionId())
                .senderEmail(response.getSenderEmail())
                .recipientEmail(response.getRecipientEmail())
                .amount(response.getAmount())
                .currency(response.getCurrency())
                .idempotencyKey(idempotencyKey)
                .completedAt(response.getCompletedAt().atZone(ZoneOffset.UTC).toInstant())
                .build();
    }

    /**
     * The actual atomic transfer work. Runs inside the TransactionTemplate callback,
     * which provides the @Transactional boundary. All DB operations commit together
     * or rollback together.
     */
    private TransferResponse performTransfer(
            String idempotencyKey,
            String senderEmail,
            TransferRequest request) {

        // === PHASE 2: Load and validate ===

        Wallet senderWallet = walletRepository.findByUserEmail(senderEmail)
                .orElseThrow(() -> new WalletNotFoundException(senderEmail));

        User recipientUser = userRepository.findByEmail(request.getRecipientEmail())
                .orElseThrow(() -> new RecipientNotFoundException(request.getRecipientEmail()));

        Wallet recipientWallet = walletRepository.findByUser(recipientUser)
                .orElseThrow(() -> new WalletNotFoundException(request.getRecipientEmail()));

        // Business rule: can't transfer to yourself
        if (senderWallet.getId().equals(recipientWallet.getId())) {
            throw new SelfTransferException();
        }

        // Business rule: sender must have enough balance
        BigDecimal amount = request.getAmount();
        if (senderWallet.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException(senderWallet.getBalance(), amount);
        }

        // === PHASE 3: Atomic execution ===

        senderWallet.setBalance(senderWallet.getBalance().subtract(amount));
        recipientWallet.setBalance(recipientWallet.getBalance().add(amount));

        Transaction txn = Transaction.builder()
                .sender(senderWallet.getUser())
                .recipient(recipientUser)
                .amount(amount)
                .currency("INR")
                .idempotencyKey(idempotencyKey)
                .status("COMPLETED")
                .build();

        Transaction savedTxn = transactionRepository.save(txn);

        // JPA dirty-checking will auto-UPDATE wallets on commit.

        log.info("Transfer completed: transactionId={} senderEmail={} recipientEmail={} amount={}",
                savedTxn.getId(), senderEmail, request.getRecipientEmail(), amount);

        // === PHASE 4: Build response ===

        return TransferResponse.builder()
                .transactionId(savedTxn.getId())
                .senderEmail(senderEmail)
                .recipientEmail(request.getRecipientEmail())
                .amount(amount)
                .currency("INR")
                .status("COMPLETED")
                .newSenderBalance(senderWallet.getBalance())
                .completedAt(LocalDateTime.now())
                .build();
    }
}