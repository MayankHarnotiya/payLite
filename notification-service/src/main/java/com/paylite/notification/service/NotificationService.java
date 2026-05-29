package com.paylite.notification.service;

import com.paylite.events.TransferCompletedEvent;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

/**
 * Placeholder for real channels (email, SMS, push).
 * For learning, we log what would be sent — easy to verify in the console.
 */
@Service
@Slf4j
public class NotificationService {

    public void notifyTransferCompleted(TransferCompletedEvent event) {
        log.info("""
                [NOTIFICATION] Transfer completed
                  transactionId : {}
                  from          : {}
                  to            : {}
                  amount        : {} {}
                  idempotencyKey: {}
                """,
                event.getTransactionId(),
                event.getSenderEmail(),
                event.getRecipientEmail(),
                event.getAmount(),
                event.getCurrency(),
                event.getIdempotencyKey());

        // Future: emailService.send(...), pushService.send(...), etc.
    }
}
