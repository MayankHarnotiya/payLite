package com.paylite.wallet.exception;

import java.math.BigDecimal;

/**
 * Thrown when a transfer is attempted but the sender's balance is too low.
 * Maps to HTTP 400 Bad Request — the client could resolve this by reducing
 * the amount or adding funds to their wallet.
 */
public class InsufficientBalanceException extends RuntimeException {

  public InsufficientBalanceException(BigDecimal available, BigDecimal requested) {
    super(String.format("Insufficient balance. Available: %s, Requested: %s",
            available, requested));
  }
}