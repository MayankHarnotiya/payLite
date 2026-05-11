package com.paylite.wallet.exception;

/**
 * Thrown when a wallet lookup fails.
 *
 * Shouldn't happen for authenticated users — auto-create on signup guarantees
 * every user has a wallet. But if it does (e.g., data corruption, race condition
 * during the auto-create transaction), we throw this and return HTTP 404.
 */
public class WalletNotFoundException extends RuntimeException {

    public WalletNotFoundException(String email) {
        super("Wallet not found for user: " + email);
    }
}