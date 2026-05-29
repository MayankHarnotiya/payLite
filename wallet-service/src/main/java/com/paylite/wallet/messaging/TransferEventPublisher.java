package com.paylite.wallet.messaging;

import com.paylite.events.TransferCompletedEvent;
import com.paylite.events.TransferTopics;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

/**
 * Publishes transfer facts to Kafka after the database transaction has committed.
 *
 * Fire-and-forget: we log failures but do not roll back the transfer.
 * (Production systems often use transactional outbox for stronger guarantees — see KAFKA guide in chat.)
 */
@Component
@ConditionalOnProperty(name = "paylite.kafka.enabled", havingValue = "true")
@RequiredArgsConstructor
@Slf4j
public class TransferEventPublisher {

    private final KafkaTemplate<String, TransferCompletedEvent> kafkaTemplate;

    @Value("${paylite.kafka.topic.transfer-completed:" + TransferTopics.TRANSFER_COMPLETED + "}")
    private String transferCompletedTopic;

    public void publish(TransferCompletedEvent event) {
        String key = String.valueOf(event.getTransactionId());

        CompletableFuture<SendResult<String, TransferCompletedEvent>> future =
                kafkaTemplate.send(transferCompletedTopic, key, event);

        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to publish TransferCompletedEvent transactionId={}: {}",
                        event.getTransactionId(), ex.getMessage(), ex);
                return;
            }
            log.info("Published TransferCompletedEvent transactionId={} partition={} offset={}",
                    event.getTransactionId(),
                    result.getRecordMetadata().partition(),
                    result.getRecordMetadata().offset());
        });
    }
}
