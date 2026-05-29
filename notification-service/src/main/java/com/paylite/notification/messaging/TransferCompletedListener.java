package com.paylite.notification.messaging;

import com.paylite.events.TransferCompletedEvent;
import com.paylite.events.TransferTopics;
import com.paylite.notification.service.NotificationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * Kafka consumer: reacts to transfer-completed facts from wallet-service.
 *
 * group-id "notification-service" means:
 *   - multiple instances share work (each partition processed by one instance)
 *   - Kafka tracks progress via committed offsets
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TransferCompletedListener {

    private final NotificationService notificationService;

    @KafkaListener(
            topics = TransferTopics.TRANSFER_COMPLETED,
            groupId = "notification-service",
            containerFactory = "transferEventKafkaListenerContainerFactory"
    )
    public void onTransferCompleted(
            @Payload TransferCompletedEvent event,
            @Header(KafkaHeaders.RECEIVED_KEY) String key,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {

        log.info("Received TransferCompletedEvent key={} partition={} offset={} transactionId={}",
                key, partition, offset, event.getTransactionId());

        try {
            notificationService.notifyTransferCompleted(event);
            acknowledgment.acknowledge();
        } catch (Exception ex) {
            // No ack → message will be redelivered (at-least-once semantics)
            log.error("Failed to process TransferCompletedEvent transactionId={}: {}",
                    event.getTransactionId(), ex.getMessage(), ex);
            throw ex;
        }
    }
}
