package com.paylite.wallet;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Full integration test: boots the entire Spring Boot app and exercises
 * the transfer flow end-to-end through HTTP, security, JPA, Redis, and
 * the database.
 *
 * Uses the already-running paylite-mysql and paylite-redis containers from
 * docker-compose. Test data uses unique UUID-based emails to avoid collisions
 * with other test runs or manual test data.
 *
 * What this test exercises (without mocking anything):
 *   - HTTP routing through Spring Security filter chain
 *   - JwtAuthenticationFilter parses real JWTs
 *   - Real BCrypt hashing of passwords
 *   - Real JPA entity persistence (users, wallets, transactions)
 *   - Real Hibernate @Version optimistic locking
 *   - Real Flyway-managed schema
 *   - Real Redis-based idempotency via IdempotencyService
 *   - Real DB UNIQUE constraint on idempotency_key
 *   - The full TransferService five-phase flow
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class TransferIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Use the already-running paylite-mysql + paylite-redis containers from docker-compose.
     * Same pragmatic Windows-friendly approach as AbstractIntegrationTest.
     */
    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url",
                () -> "jdbc:mysql://localhost:3307/paylite_wallet?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC");
        registry.add("spring.datasource.username", () -> "paylite");
        registry.add("spring.datasource.password", () -> "paylite_dev_password");
        registry.add("spring.data.redis.host", () -> "localhost");
        registry.add("spring.data.redis.port", () -> "6379");
    }

    @Test
    @DisplayName("Full end-to-end: signup x2 -> login -> add money -> transfer -> idempotent retry")
    void shouldCompleteFullTransferFlowEndToEnd() throws Exception {

        // === SETUP: unique test users for this run ===
        String runId = UUID.randomUUID().toString().substring(0, 8);
        String senderEmail = "it-sender-" + runId + "@paylite.com";
        String recipientEmail = "it-recipient-" + runId + "@paylite.com";
        String idempotencyKey = "it-key-" + UUID.randomUUID();

        // === STEP 1: sign up sender ===
        ResponseEntity<String> senderSignup = post("/api/auth/signup", null,
                "{\"email\":\"" + senderEmail + "\"," +
                        "\"password\":\"secret123\"," +
                        "\"fullName\":\"IT Sender\"," +
                        "\"phone\":\"9876543210\"}");
        assertThat(senderSignup.getStatusCode().value())
                .as("Sender signup should return 201")
                .isEqualTo(201);

        // === STEP 2: sign up recipient ===
        ResponseEntity<String> recipientSignup = post("/api/auth/signup", null,
                "{\"email\":\"" + recipientEmail + "\"," +
                        "\"password\":\"secret123\"," +
                        "\"fullName\":\"IT Recipient\"," +
                        "\"phone\":\"9876543211\"}");
        assertThat(recipientSignup.getStatusCode().value())
                .as("Recipient signup should return 201")
                .isEqualTo(201);

        // === STEP 3: login as sender, extract JWT ===
        ResponseEntity<String> loginResponse = post("/api/auth/login", null,
                "{\"email\":\"" + senderEmail + "\",\"password\":\"secret123\"}");
        assertThat(loginResponse.getStatusCode().value())
                .as("Login should return 200")
                .isEqualTo(200);

        JsonNode loginJson = objectMapper.readTree(loginResponse.getBody());
        String token = loginJson.get("accessToken").asText();
        assertThat(token).as("JWT should be present").isNotBlank();

        // === STEP 4: add money to sender's wallet ===
        ResponseEntity<String> addMoneyResponse = post("/api/wallets/add-money", token,
                "{\"amount\":\"500.00\"}");
        assertThat(addMoneyResponse.getStatusCode().value()).isEqualTo(200);

        JsonNode walletJson = objectMapper.readTree(addMoneyResponse.getBody());
        assertThat(walletJson.get("balance").decimalValue())
                .as("Sender balance after add-money should be 500")
                .isEqualByComparingTo("500.00");

        // === STEP 5: first transfer attempt — should succeed ===
        ResponseEntity<String> transfer1 = transfer(token, idempotencyKey,
                "{\"recipientEmail\":\"" + recipientEmail + "\"," +
                        "\"amount\":\"100.00\"}");

        assertThat(transfer1.getStatusCode().value())
                .as("First transfer should return 200")
                .isEqualTo(200);

        JsonNode txn1 = objectMapper.readTree(transfer1.getBody());
        assertThat(txn1.get("status").asText()).isEqualTo("COMPLETED");
        assertThat(txn1.get("amount").decimalValue()).isEqualByComparingTo("100.00");
        assertThat(txn1.get("newSenderBalance").decimalValue())
                .as("Sender balance after transfer should be 400")
                .isEqualByComparingTo("400.00");
        assertThat(txn1.get("senderEmail").asText()).isEqualTo(senderEmail);
        assertThat(txn1.get("recipientEmail").asText()).isEqualTo(recipientEmail);

        long firstTransactionId = txn1.get("transactionId").asLong();
        String firstCompletedAt = txn1.get("completedAt").asText();

        // === STEP 6: idempotency replay — SAME key, expect SAME response ===
        ResponseEntity<String> transfer2 = transfer(token, idempotencyKey,
                "{\"recipientEmail\":\"" + recipientEmail + "\"," +
                        "\"amount\":\"100.00\"}");

        assertThat(transfer2.getStatusCode().value())
                .as("Idempotency replay should also return 200")
                .isEqualTo(200);

        JsonNode txn2 = objectMapper.readTree(transfer2.getBody());

        // CRITICAL: same transaction ID, same timestamp = NO new processing happened
        assertThat(txn2.get("transactionId").asLong())
                .as("Idempotency replay should return SAME transactionId")
                .isEqualTo(firstTransactionId);
        assertThat(txn2.get("completedAt").asText())
                .as("Idempotency replay should return SAME completedAt")
                .isEqualTo(firstCompletedAt);
        assertThat(txn2.get("newSenderBalance").decimalValue())
                .as("Balance should still be 400 (not 300)")
                .isEqualByComparingTo("400.00");
    }

    // === Helpers ===

    private ResponseEntity<String> post(String path, String bearerToken, String jsonBody) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (bearerToken != null) {
            headers.setBearerAuth(bearerToken);
        }
        HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);
        return restTemplate.exchange(
                "http://localhost:" + port + path,
                HttpMethod.POST,
                entity,
                String.class);
    }

    private ResponseEntity<String> transfer(String bearerToken, String idempotencyKey, String jsonBody) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(bearerToken);
        headers.set("Idempotency-Key", idempotencyKey);
        HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);
        return restTemplate.exchange(
                "http://localhost:" + port + "/api/wallets/transfer",
                HttpMethod.POST,
                entity,
                String.class);
    }
}