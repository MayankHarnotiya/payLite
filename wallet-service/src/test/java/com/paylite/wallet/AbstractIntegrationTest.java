package com.paylite.wallet;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * Base class for integration tests that need a real MySQL database.
 *
 * Originally written to use Testcontainers, but switched to using the
 * already-running paylite-mysql Docker container (started via docker-compose)
 * because of Windows + Docker Desktop + WSL2 npipe communication issues
 * during the Testcontainers handshake.
 *
 * Pragmatic trade-off:
 *   - Pro: tests run immediately, no Docker debugging
 *   - Pro: same MySQL 8.0 image as production, same Flyway migrations
 *   - Con: tests share data namespace with manual testing; we use unique
 *          email prefixes per test to avoid collisions
 *
 * In production CI/CD (e.g., GitHub Actions on Linux), the original
 * Testcontainers version would work fine. This is a Windows-specific workaround.
 */
public abstract class AbstractIntegrationTest {

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        // Point tests at the already-running paylite-mysql container
        // (started via `docker compose up -d` from the project root)
        registry.add("spring.datasource.url",
                () -> "jdbc:mysql://localhost:3307/paylite_wallet?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC");
        registry.add("spring.datasource.username", () -> "paylite");
        registry.add("spring.datasource.password", () -> "paylite_dev_password");
        registry.add("spring.datasource.driver-class-name", () -> "com.mysql.cj.jdbc.Driver");
        // Flyway migrations have already been applied to this DB
        registry.add("spring.flyway.enabled", () -> "true");
        registry.add("spring.flyway.locations", () -> "classpath:db/migration");
    }
}