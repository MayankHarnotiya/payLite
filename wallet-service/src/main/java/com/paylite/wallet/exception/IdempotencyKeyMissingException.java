package com.paylite.wallet.exception;

/**
 * Thrown when a state-changing request arrives without an Idempotency-Key header.
 * Maps to HTTP 400 Bad Request.
 *
 * We REQUIRE idempotency keys for transfers — even if the client doesn't intend
 * to retry, sending the key forces good habits and prevents accidental
 * double-processing on network blips.
 */
public class IdempotencyKeyMissingException extends RuntimeException {

    public IdempotencyKeyMissingException() {
        super("Idempotency-Key header is required for this operation");
    }
}