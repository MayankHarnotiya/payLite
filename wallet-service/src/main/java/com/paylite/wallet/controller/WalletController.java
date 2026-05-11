package com.paylite.wallet.controller;

import com.paylite.wallet.dto.AddMoneyRequest;
import com.paylite.wallet.dto.WalletResponse;
import com.paylite.wallet.service.WalletService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * HTTP endpoints for wallet operations.
 *
 * All endpoints require authentication. The current user is always derived from
 * the JWT (via @AuthenticationPrincipal) — never trusted from URL or body.
 */
@RestController
@RequestMapping("/api/wallets")
@RequiredArgsConstructor
@Slf4j
public class WalletController {

    private final WalletService walletService;

    /**
     * GET /api/wallets/me — get the current user's wallet.
     */
    @GetMapping("/me")
    public WalletResponse getMyWallet(@AuthenticationPrincipal UserDetails user) {
        log.debug("GET /api/wallets/me for email={}", user.getUsername());
        return walletService.getWalletByEmail(user.getUsername());
    }

    /**
     * POST /api/wallets/add-money — credit the current user's wallet.
     *
     * In real production this would be triggered by a payment gateway webhook
     * (e.g., Razorpay's payment.captured event). For the MVP, the authenticated
     * user calls this directly.
     */
    @PostMapping("/add-money")
    public WalletResponse addMoney(
            @AuthenticationPrincipal UserDetails user,
            @Valid @RequestBody AddMoneyRequest request) {

        log.debug("POST /api/wallets/add-money for email={} amount={}",
                user.getUsername(), request.getAmount());
        return walletService.addMoney(user.getUsername(), request);
    }
}