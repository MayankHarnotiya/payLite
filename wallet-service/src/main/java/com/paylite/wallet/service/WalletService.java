package com.paylite.wallet.service;

import com.paylite.wallet.dto.AddMoneyRequest;
import com.paylite.wallet.dto.TransactionHistoryResponse;
import com.paylite.wallet.dto.TransactionItemResponse;
import com.paylite.wallet.dto.WalletResponse;
import com.paylite.wallet.entity.Transaction;
import com.paylite.wallet.entity.User;
import com.paylite.wallet.entity.Wallet;
import com.paylite.wallet.exception.WalletNotFoundException;
import com.paylite.wallet.repository.TransactionRepository;
import com.paylite.wallet.repository.UserRepository;
import com.paylite.wallet.repository.WalletRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Business logic for wallet operations.
 *
 * Methods scoped by EMAIL from the JWT (passed by controller).
 * Caller can never operate on another user's wallet.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WalletService {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    /**
     * Read the current user's wallet. Read-only transaction = small Hibernate perf boost.
     */
    @Transactional(readOnly = true)
    public WalletResponse getWalletByEmail(String email) {
        log.debug("Fetching wallet for email={}", email);

        Wallet wallet = walletRepository.findByUserEmail(email)
                .orElseThrow(() -> new WalletNotFoundException(email));

        return toResponse(wallet);
    }

    /**
     * Credit money to the current user's wallet.
     *
     * The whole method runs in a transaction:
     *   1. Read the wallet (locks the row at the JPA level via @Version)
     *   2. Compute newBalance = currentBalance + amount (BigDecimal arithmetic)
     *   3. Set the new balance
     *   4. Transaction commits → Hibernate issues UPDATE wallets SET balance=?, version=?
     *      WHERE id=? AND version=? (optimistic lock check)
     *
     * If another transaction modified this wallet in parallel, the version check
     * would fail → OptimisticLockException.
     */
    @Transactional
    public WalletResponse addMoney(String email, AddMoneyRequest request) {
        log.info("Credit attempt: email={} amount={}", email, request.getAmount());

        Wallet wallet = walletRepository.findByUserEmail(email)
                .orElseThrow(() -> new WalletNotFoundException(email));

        BigDecimal newBalance = wallet.getBalance().add(request.getAmount());
        wallet.setBalance(newBalance);

        // No explicit save() needed — within a @Transactional method, JPA's "dirty checking"
        // detects the change and issues UPDATE at commit time.

        log.info("Wallet credited: email={} newBalance={}", email, newBalance);
        return toResponse(wallet);
    }

    /**
     * Returns the paginated transaction history for the given user.
     * Includes both sent and received transactions, newest first.
     *
     * The findUserHistory query uses JOIN FETCH on sender + recipient
     * to avoid N+1 queries when building the response DTOs.
     */
    @Transactional(readOnly = true)
    public TransactionHistoryResponse getTransactionHistory(String email, int page, int size) {
        log.info("Fetching transaction history: email={} page={} size={}", email, page, size);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new WalletNotFoundException(email));

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Transaction> txnPage = transactionRepository.findUserHistory(user, pageable);

        List<TransactionItemResponse> items = txnPage.getContent().stream()
                .map(txn -> TransactionItemResponse.from(txn, user))
                .toList();

        log.info("Transaction history fetched: email={} totalElements={}", email, txnPage.getTotalElements());

        return TransactionHistoryResponse.builder()
                .content(items)
                .page(txnPage.getNumber())
                .size(txnPage.getSize())
                .totalElements(txnPage.getTotalElements())
                .totalPages(txnPage.getTotalPages())
                .build();
    }

    /**
     * Private helper to convert entity → DTO.
     * Centralized so we don't repeat the mapping in both getWallet and addMoney.
     */
    private WalletResponse toResponse(Wallet wallet) {
        return WalletResponse.builder()
                .balance(wallet.getBalance())
                .currency("INR")
                .lastUpdated(wallet.getUpdatedAt())
                .build();
    }
}