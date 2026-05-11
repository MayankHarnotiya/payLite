package com.paylite.wallet.service;

import com.paylite.wallet.dto.AddMoneyRequest;
import com.paylite.wallet.dto.WalletResponse;
import com.paylite.wallet.entity.Wallet;
import com.paylite.wallet.exception.WalletNotFoundException;
import com.paylite.wallet.repository.WalletRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

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
     * would fail → OptimisticLockException. We'll handle that gracefully in Day 7.
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