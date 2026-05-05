-- V1__create_users_table.sql
-- Creates the foundational users table.
-- Flyway runs this exactly once and records it in the flyway_schema_history table.

CREATE TABLE users (
                       id              BIGINT          NOT NULL AUTO_INCREMENT,
                       email           VARCHAR(100)    NOT NULL,
                       password_hash   VARCHAR(255)    NOT NULL,
                       full_name       VARCHAR(100)    NOT NULL,
                       phone           VARCHAR(15)     NULL,
                       created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

                       PRIMARY KEY (id),
                       UNIQUE KEY uk_users_email (email),
                       INDEX idx_users_created_at (created_at)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;