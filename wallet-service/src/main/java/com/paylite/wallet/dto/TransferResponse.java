package com.paylite.wallet.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Output shape for POST /api/wallets/transfer.
 *
 * Returns enough info for the client to:
 *   - confirm the transfer succeeded
 *   - display the new sender balance immediately (avoid a separate GET /me)
 *   - record the transactionId for support reference
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransferResponse {

    private Long transactionId;
    private String senderEmail;
    private String recipientEmail;
    private BigDecimal amount;
    private String currency;
    private String status;          // typically "COMPLETED"
    private BigDecimal newSenderBalance;
    private LocalDateTime completedAt;
}