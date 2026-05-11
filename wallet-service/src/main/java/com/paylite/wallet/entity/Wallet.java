package com.paylite.wallet.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Represents a user's wallet — their PayLite balance.
 * Maps 1:1 to the `wallets` table created by Flyway migration V2.
 *
 * Each User has exactly ONE Wallet (enforced by UNIQUE KEY on user_id).
 * Balance is always >= 0 (enforced by DB CHECK constraint AND application logic).
 *
 * @Version field enables optimistic locking — critical for race-free transfers
 * tomorrow.
 */
@Entity
@Table(name = "wallets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The user who owns this wallet.
     * @OneToOne with FetchType.LAZY = don't auto-load the User unless we ask for it.
     * @JoinColumn says "the foreign key column is `user_id`".
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    /**
     * Current balance. NEVER use double or float for money.
     * BigDecimal preserves exact decimal arithmetic.
     */
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal balance;

    /**
     * Optimistic locking version.
     * On every UPDATE, Hibernate increments this and checks the WHERE clause includes
     * the previous version. If two transactions try to update the same row, only the
     * first wins; the second gets OptimisticLockException.
     * Critical for safe concurrent money operations.
     */
    @Version
    @Column(nullable = false)
    private Long version;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}