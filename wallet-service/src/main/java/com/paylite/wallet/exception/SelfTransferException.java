package com.paylite.wallet.exception;

/**
 * Thrown when a user tries to transfer money to themselves.
 * Maps to HTTP 400 Bad Request.
 *
 * Why guard against this? It would be a no-op accounting-wise (debit and credit
 * the same wallet) but would still:
 *   - waste resources (DB roundtrips, transaction log entries)
 *   - confuse users seeing "I sent money to myself" in their history
 *   - potentially exploit a race condition involving optimistic locking
 */
public class SelfTransferException extends RuntimeException {

    public SelfTransferException() {
        super("Cannot transfer money to yourself");
    }
}