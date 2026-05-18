package com.paylite.wallet;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Base class for integration tests that need a real MySQL database.
 *
 * Spins up a single MySQL 8.0 container shared across all tests in the JVM.
 * Flyway runs the V1, V2, V3 migrations on container startup, so the schema
 * is identical to production.
 *
 * Test classes that extend this get a real DB connection without managing it.
 */
@Testcontainers
public abstract class AbstractIntegrationTest {

    @Container
    protected static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("paylite_test")
            .withUsername("paylite_test")
            .withPassword("paylite_test_pwd")
            .withReuse(true);  // reuse container across test runs for speed (Testcontainers feature)

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MYSQL::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL::getUsername);
        registry.add("spring.datasource.password", MYSQL::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "com.mysql.cj.jdbc.Driver");
        // Flyway runs migrations against the real container DB
        registry.add("spring.flyway.enabled", () -> "true");
        registry.add("spring.flyway.locations", () -> "classpath:db/migration");
    }
}