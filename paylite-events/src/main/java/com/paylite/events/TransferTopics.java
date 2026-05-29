package com.paylite.events;

/**
 * Central place for Kafka topic names.
 * One constant avoids typos between producer and consumer.
 */
public final class TransferTopics {

    public static final String TRANSFER_COMPLETED = "paylite.transfer.completed";

    private TransferTopics() {
    }
}
