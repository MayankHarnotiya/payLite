package com.paylite.wallet.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Input shape for POST /api/wallets/add-money.
 *
 * Validation constraints:
 *   - amount must be present (not null)
 *   - amount must be > 0 (inclusive=false means strictly greater)
 *   - amount must be ≤ 1,000,000 (10 lakh — sane upper limit per single credit)
 *   - amount must have at most 2 decimal places (paise precision)
 *
 * Note: we accept BigDecimal directly. Jackson deserializes JSON numbers to
 * BigDecimal when the field type is BigDecimal — preserving exact precision.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddMoneyRequest {

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    @DecimalMax(value = "1000000.00", message = "Amount cannot exceed 10,00,000")
    @Digits(integer = 7, fraction = 2, message = "Amount must have at most 2 decimal places")
    private BigDecimal amount;
}