package com.paylite.wallet.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paylite.wallet.exception.ConcurrentRetryException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Generic idempotency mechanism using Redis SET NX EX.
 *
 * The claim-or-retry pattern:
 *   1. Try to atomically claim a key with status "PENDING".
 *   2. If claim succeeds → we are the first request. Execute the operation,
 *      cache the response (overwrite PENDING), and return.
 *   3. If claim fails → another request owns the key. Read what's there:
 *        - "PENDING"  → first request still running. Throw ConcurrentRetryException.
 *        - JSON       → first request completed. Return the cached response.
 *
 * Why Redis? Sub-ms lookups, automatic TTL cleanup, and atomic SET NX guarantees.
 * The DB has a UNIQUE constraint on idempotency_key as a last-line defense.
 *
 * Note: this service is RESPONSE-TYPE-AGNOSTIC. It serializes/deserializes
 * responses via Jackson, so any DTO can be cached.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class IdempotencyService {

    private static final String KEY_PREFIX = "idempotency:";
    private static final String PENDING_MARKER = "PENDING";
    private static final Duration TTL = Duration.ofHours(24);

    private final StringRedisTemplate redis;
    private final ObjectMapper objectMapper;

    /**
     * Execute an operation idempotently.
     *
     * @param idempotencyKey  client-supplied unique key
     * @param responseType    the expected response type (for deserializing cached responses)
     * @param operation       the actual work to perform (only runs on first request)
     * @return either a fresh response or the cached response from a prior request
     */
    public <T> T executeIdempotent(
            String idempotencyKey,
            Class<T> responseType,
            Supplier<T> operation) {

        String redisKey = KEY_PREFIX + idempotencyKey;

        // Step 1: Try to atomically claim the key.
        Boolean claimed = redis.opsForValue().setIfAbsent(redisKey, PENDING_MARKER, TTL);

        // setIfAbsent returns Boolean (can be null in rare connection errors); handle defensively
        if (Boolean.TRUE.equals(claimed)) {
            log.debug("Idempotency key claimed: {}", idempotencyKey);

            T response;
            try {
                // We're the first. Execute the actual operation.
                response = operation.get();
            } catch (RuntimeException ex) {
                // On any failure, DELETE the PENDING marker so retries can proceed cleanly.
                // Otherwise the retry would see "PENDING" forever (until TTL expires).
                log.warn("Operation failed, releasing idempotency claim: {}", idempotencyKey);
                redis.delete(redisKey);
                throw ex;
            }

            // Operation succeeded. Cache the serialized response (overwrite PENDING).
            String serialized = serialize(response);
            redis.opsForValue().set(redisKey, serialized, TTL);
            log.debug("Cached response for idempotency key: {}", idempotencyKey);

            return response;
        }

        // Step 2: Key already exists. Inspect what's cached.
        return handleExistingKey(redisKey, idempotencyKey, responseType, operation);
    }

    private <T> T handleExistingKey(
            String redisKey,
            String idempotencyKey,
            Class<T> responseType,
            Supplier<T> operation) {

        String cached = redis.opsForValue().get(redisKey);

        if (cached == null) {
            // Race: key expired between setIfAbsent (which saw it exists) and get (which saw it gone).
            // Extremely rare. Fall through to a fresh claim attempt.
            log.warn("Idempotency key expired between claim and read; retrying: {}", idempotencyKey);
            return executeIdempotent(idempotencyKey, responseType, operation);
        }

        if (PENDING_MARKER.equals(cached)) {
            // Another request is still processing. Tell the client to retry shortly.
            log.warn("Concurrent retry for idempotency key still in PENDING state: {}", idempotencyKey);
            throw new ConcurrentRetryException();
        }

        // We have a previously-cached completed response. Deserialize and return it.
        log.debug("Returning cached idempotent response for key: {}", idempotencyKey);
        return deserialize(cached, responseType);
    }

    /**
     * Look up a previously-cached response without consuming the slot.
     * Useful if a caller wants to peek before committing to executeIdempotent.
     */
    public <T> Optional<T> getCachedResponse(String idempotencyKey, Class<T> responseType) {
        String cached = redis.opsForValue().get(KEY_PREFIX + idempotencyKey);
        if (cached == null || PENDING_MARKER.equals(cached)) {
            return Optional.empty();
        }
        return Optional.of(deserialize(cached, responseType));
    }

    private <T> String serialize(T value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to serialize idempotency response", ex);
        }
    }

    private <T> T deserialize(String json, Class<T> type) {
        try {
            return objectMapper.readValue(json, type);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to deserialize cached idempotency response", ex);
        }
    }
}