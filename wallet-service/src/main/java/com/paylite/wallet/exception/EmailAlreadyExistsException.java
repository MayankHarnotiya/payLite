package com.paylite.wallet.exception;

/**
 * Thrown by UserService when a signup attempt uses an email that's already registered.
 *
 * Extends RuntimeException (not checked Exception) so we don't have to declare
 * `throws ...` everywhere. This is the modern Spring convention — checked exceptions
 * are considered bad ergonomics for application errors.
 *
 * In Step 10 we'll add a global handler that catches this exception and returns
 * HTTP 409 Conflict with a clean JSON error body.
 */
public class EmailAlreadyExistsException extends RuntimeException {

    public EmailAlreadyExistsException(String email) {
        super("Email is already registered: " + email);
    }
}