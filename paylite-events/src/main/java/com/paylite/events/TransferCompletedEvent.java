package com.paylite.events;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Emitted after a P2P transfer commits successfully in MySQL.
 * Serialized to JSON on the Kafka topic {@link TransferTopics#TRANSFER_COMPLETED}.
 *
 * This is a fact ("transfer happened"), not a command ("send email").
 * Consumers decide what to do — email, SMS, push, analytics, etc.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TransferCompletedEvent {

    private Long transactionId;
    private String senderEmail;
    private String recipientEmail;
    private BigDecimal amount;
    private String currency;
    private String idempotencyKey;
    private Instant completedAt;
}
