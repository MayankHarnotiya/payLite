package com.paylite.wallet.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Output shape for wallet endpoints.
 *
 * Returns just the data the client needs:
 *   - balance (with currency for clarity)
 *   - lastUpdated so clients can show "as of HH:MM:SS"
 *
 * Deliberately omits:
 *   - id (internal database concern)
 *   - userId (the client knows it's their own wallet)
 *   - version (internal optimistic-locking concern)
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WalletResponse {

    private BigDecimal balance;
    private String currency;
    private LocalDateTime lastUpdated;
}