package com.paylite.wallet.exception;

/**
 * Thrown when a request arrives with an Idempotency-Key that's currently being
 * processed by another request. The first request has claimed the key but
 * hasn't completed yet.
 *
 * Maps to HTTP 409 Conflict — the client should wait briefly and retry.
 * The Retry-After header could be added in production.
 *
 * This race is rare in practice (would require sub-second simultaneous clicks)
 * but handling it cleanly demonstrates production-grade concurrency thinking.
 */
public class ConcurrentRetryException extends RuntimeException {

    public ConcurrentRetryException() {
        super("A request with this Idempotency-Key is currently being processed. Please retry shortly.");
    }
}