package com.paylite.wallet.exception;

/**
 * Thrown when login fails — either email doesn't exist OR password is wrong.
 *
 * IMPORTANT: we never tell the client WHICH part is wrong.
 * "Invalid credentials" only — same response for both cases.
 *
 * Why? Telling the client "user not found" lets attackers enumerate which
 * emails are registered (a privacy leak). "Invalid credentials" is ambiguous
 * and gives away nothing.
 */
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException() {
        super("Invalid email or password");
    }
}