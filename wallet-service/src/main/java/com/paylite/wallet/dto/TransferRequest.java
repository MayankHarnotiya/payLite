package com.paylite.wallet.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Input shape for POST /api/wallets/transfer.
 *
 * Note: idempotency key comes via header (Idempotency-Key), NOT in this body.
 * Same payload + different idempotency key = treated as two separate requests.
 * Same payload + same key = treated as a retry (returned cached response).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransferRequest {

    @NotBlank(message = "Recipient email is required")
    @Email(message = "Recipient email format is invalid")
    private String recipientEmail;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    @DecimalMax(value = "1000000.00", message = "Amount cannot exceed 10,00,000")
    @Digits(integer = 7, fraction = 2, message = "Amount must have at most 2 decimal places")
    private BigDecimal amount;
}