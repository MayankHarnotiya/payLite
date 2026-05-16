package com.paylite.wallet.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import org.hibernate.annotations.CreationTimestamp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Append-only ledger entry for every transfer attempt — successful or failed.
 * Maps 1:1 to the `transactions` table created by Flyway migration V3.
 *
 * Once written, transactions are NEVER modified. The wallets table holds
 * current balance; this table holds how we got there. Audit trail.
 *
 * Two relationships to User:
 *   - sender:    who's paying (the wallet being debited)
 *   - recipient: who's receiving (the wallet being credited)
 * Both LAZY — we only navigate them when needed.
 */
@Entity
@Table(name = "transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The user being debited.
     * @ManyToOne — many transactions can have the same sender.
     * LAZY = don't load the User unless we ask for it.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    /**
     * The user being credited.
     * @ManyToOne — many transactions can have the same recipient.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", nullable = false)
    private User recipient;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    /**
     * Client-supplied idempotency key. UNIQUE in the DB — physical guarantee
     * against double-processing the same logical request.
     */
    @Column(name = "idempotency_key", nullable = false, length = 64, unique = true)
    private String idempotencyKey;

    /**
     * Transaction state: COMPLETED, FAILED, or PENDING.
     * Stored as String; DB CHECK constraint validates the allowed values.
     * Could be refactored to an enum later if behavior is added per status.
     */
    @Column(nullable = false, length = 20)
    private String status;

    /**
     * Why the transfer failed (null for COMPLETED transactions).
     * Examples: "Insufficient balance", "Recipient not found", "System error".
     */
    @Column(name = "failure_reason", length = 255)
    private String failureReason;

    /**
     * When this row was recorded. Immutable — no @UpdateTimestamp here.
     * Transactions never change after creation.
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}