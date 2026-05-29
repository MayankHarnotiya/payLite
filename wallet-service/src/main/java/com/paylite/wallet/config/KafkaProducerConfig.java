package com.paylite.wallet.config;

import com.paylite.events.TransferCompletedEvent;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

/**
 * Wires Spring's KafkaTemplate for publishing {@link TransferCompletedEvent}.
 * Only loaded when paylite.kafka.enabled=true (off in unit tests by default).
 */
@Configuration
@ConditionalOnProperty(name = "paylite.kafka.enabled", havingValue = "true")
public class KafkaProducerConfig {

    @Bean
    public ProducerFactory<String, TransferCompletedEvent> transferEventProducerFactory(
            KafkaProperties kafkaProperties) {

        Map<String, Object> config = new HashMap<>(kafkaProperties.buildProducerProperties(null));
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        // Include type info in headers so consumers can deserialize safely (Spring default)
        config.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);

        return new DefaultKafkaProducerFactory<>(config);
    }

    @Bean
    public KafkaTemplate<String, TransferCompletedEvent> transferEventKafkaTemplate(
            ProducerFactory<String, TransferCompletedEvent> transferEventProducerFactory) {

        return new KafkaTemplate<>(transferEventProducerFactory);
    }
}
