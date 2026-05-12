-- V3__create_transactions_table.sql
-- Append-only transaction ledger. Every transfer attempt — successful or failed —
-- gets a row. The wallets table is the current state; this table is the history.

CREATE TABLE transactions (
                              id                BIGINT          NOT NULL AUTO_INCREMENT,
                              sender_id         BIGINT          NOT NULL,
                              recipient_id      BIGINT          NOT NULL,
                              amount            DECIMAL(15, 2)  NOT NULL,
                              currency          VARCHAR(3)      NOT NULL DEFAULT 'INR',
                              idempotency_key   VARCHAR(64)     NOT NULL,
                              status            VARCHAR(20)     NOT NULL,
                              failure_reason    VARCHAR(255)    NULL,
                              created_at        TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
                              PRIMARY KEY (id),
                              UNIQUE KEY uk_transactions_idem (idempotency_key),
                              CONSTRAINT fk_transactions_sender    FOREIGN KEY (sender_id)    REFERENCES users(id),
                              CONSTRAINT fk_transactions_recipient FOREIGN KEY (recipient_id) REFERENCES users(id),
                              CONSTRAINT chk_transactions_amount_pos    CHECK (amount > 0),
                              CONSTRAINT chk_transactions_status_valid  CHECK (status IN ('COMPLETED', 'FAILED', 'PENDING')),
                              INDEX idx_transactions_sender    (sender_id, created_at),
                              INDEX idx_transactions_recipient (recipient_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;