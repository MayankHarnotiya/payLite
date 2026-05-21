package com.paylite.wallet.dto;

import com.paylite.wallet.entity.Transaction;
import com.paylite.wallet.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Single transaction in the user's history.
 *
 * The "type" field (SENT/RECEIVED) is computed relative to the logged-in user:
 *   - If user is the sender   → type = SENT,     counterparty = recipient
 *   - If user is the recipient → type = RECEIVED, counterparty = sender
 *
 * This is a presentation concern, not a DB concern — the Transaction entity
 * stores sender + recipient; this DTO reshapes it for the user's perspective.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionItemResponse {

    private Long transactionId;
    private String type;               // "SENT" or "RECEIVED"
    private String counterpartyEmail;
    private String counterpartyName;
    private BigDecimal amount;
    private String currency;
    private String status;
    private LocalDateTime createdAt;

    /**
     * Static factory that transforms a Transaction entity into the logged-in
     * user's perspective. The same DB row looks different depending on whether
     * you're the sender or the recipient.
     */
    public static TransactionItemResponse from(Transaction txn, User currentUser) {
        boolean isSender = txn.getSender().getId().equals(currentUser.getId());

        User counterparty = isSender ? txn.getRecipient() : txn.getSender();

        return TransactionItemResponse.builder()
                .transactionId(txn.getId())
                .type(isSender ? "SENT" : "RECEIVED")
                .counterpartyEmail(counterparty.getEmail())
                .counterpartyName(counterparty.getFullName())
                .amount(txn.getAmount())
                .currency(txn.getCurrency())
                .status(txn.getStatus())
                .createdAt(txn.getCreatedAt())
                .build();
    }
}