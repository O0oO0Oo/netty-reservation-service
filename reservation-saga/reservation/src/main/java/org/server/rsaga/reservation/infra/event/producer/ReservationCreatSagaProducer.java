package org.server.rsaga.reservation.infra.event.producer;

import lombok.RequiredArgsConstructor;
import org.server.rsaga.messaging.adapter.producer.KafkaMessageProducer;
import org.server.rsaga.messaging.adapter.producer.factory.KafkaMessageProducerFactory;
import org.server.rsaga.messaging.adapter.producer.strategy.KafkaMessageProducerType;
import org.server.rsaga.reservation.CreateReservationEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

@Configuration
@RequiredArgsConstructor
public class ReservationCreatSagaProducer {
    private final String TRANSACTIONAL_ID = "transactional.id";
    private final String BATCH_SIZE_CONFIG = "batch.size";
    private final String LINGER_MS_CONFIG =  "linger.ms";
    private final KafkaMessageProducerFactory kafkaMessageProducerFactory;

    @Bean("userVerifyProducer")
    public KafkaMessageProducer<String, CreateReservationEvent> userVerifyProducer() {
        Properties config = new Properties();
        config.put(BATCH_SIZE_CONFIG, "1638400");
        config.put(LINGER_MS_CONFIG, "100");
        return kafkaMessageProducerFactory.createProducer(config, KafkaMessageProducerType.AT_LEAST_ONCE_BATCH);
    }

    @Bean("businessVerifyProducer")
    public KafkaMessageProducer<String, CreateReservationEvent> businessVerifyProducer() {
        Properties config = new Properties();
        config.put(BATCH_SIZE_CONFIG, "1638400");
        config.put(LINGER_MS_CONFIG, "100");
        return kafkaMessageProducerFactory.createProducer(config, KafkaMessageProducerType.AT_LEAST_ONCE_BATCH);
    }

    @Bean("reservableItemVerifyProducer")
    public KafkaMessageProducer<String, CreateReservationEvent> reservableItemVerifyProducer() {
        Properties config = new Properties();
        config.put(BATCH_SIZE_CONFIG, "1638400");
        config.put(LINGER_MS_CONFIG, "100");
        return kafkaMessageProducerFactory.createProducer(config, KafkaMessageProducerType.AT_LEAST_ONCE_BATCH);
    }
    @Bean("checkReservationLimitProducer")
    public KafkaMessageProducer<String, CreateReservationEvent> checkReservationLimitProducer() {
        Properties config = new Properties();
        config.put(BATCH_SIZE_CONFIG, "1638400");
        config.put(LINGER_MS_CONFIG, "100");
        return kafkaMessageProducerFactory.createProducer(config, KafkaMessageProducerType.AT_LEAST_ONCE_BATCH);
    }
    @Bean("paymentProducer")
    public KafkaMessageProducer<String, CreateReservationEvent> paymentProducer() {
        Properties config = new Properties();
        config.put(BATCH_SIZE_CONFIG, "1638400");
        config.put(LINGER_MS_CONFIG, "100");
        return kafkaMessageProducerFactory.createProducer(config, KafkaMessageProducerType.AT_LEAST_ONCE_BATCH);
    }
    @Bean("updateItemQuantityProducer")
    public KafkaMessageProducer<String, CreateReservationEvent> updateItemQuantityProducer() {
        Properties config = new Properties();
        config.put(BATCH_SIZE_CONFIG, "1638400");
        config.put(LINGER_MS_CONFIG, "100");
        return kafkaMessageProducerFactory.createProducer(config, KafkaMessageProducerType.AT_LEAST_ONCE_BATCH);
    }
    @Bean("createReservationFinalProducer")
    public KafkaMessageProducer<String, CreateReservationEvent> createReservationFinalProducer() {
        Properties config = new Properties();
        config.put(BATCH_SIZE_CONFIG, "1638400");
        config.put(LINGER_MS_CONFIG, "100");
        return kafkaMessageProducerFactory.createProducer(config, KafkaMessageProducerType.AT_LEAST_ONCE_BATCH);
    }
}