package com.paylite.wallet.exception;

/**
 * Thrown when the recipient email in a transfer request doesn't match any
 * registered user. Maps to HTTP 404 Not Found.
 *
 * Privacy trade-off: this DOES leak whether an email is registered on PayLite.
 * For high-privacy apps, you'd return a generic "transfer failed" instead.
 * For PayLite, UX wins — users need to know they typed the wrong email.
 */
public class RecipientNotFoundException extends RuntimeException {

    public RecipientNotFoundException(String email) {
        super("Recipient not found: " + email);
    }
}