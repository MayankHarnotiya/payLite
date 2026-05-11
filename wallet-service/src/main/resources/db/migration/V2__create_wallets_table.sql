-- V2__create_wallets_table.sql
-- Creates the wallets table with one wallet per user.

CREATE TABLE wallets (
                         id              BIGINT          NOT NULL AUTO_INCREMENT,
                         user_id         BIGINT          NOT NULL,
                         balance         DECIMAL(15, 2)  NOT NULL DEFAULT 0.00,
                         version         BIGINT          NOT NULL DEFAULT 0,
                         created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
                         updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                         PRIMARY KEY (id),
                         UNIQUE KEY uk_wallets_user_id (user_id),
                         CONSTRAINT fk_wallets_user_id FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                         CONSTRAINT chk_wallets_balance_nonneg CHECK (balance >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO wallets (user_id, balance)
SELECT u.id, 0.00
FROM users u
         LEFT JOIN wallets w ON w.user_id = u.id
WHERE w.id IS NULL;