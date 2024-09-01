package org.server.rsaga.messaging.adapter.producer.strategy;

public enum KafkaMessageProducerType {
    AT_LEAST_ONCE,
    AT_LEAST_ONCE_BATCH,
    EXACTLY_ONCE,
    EXACTLY_ONCE_BATCH
}
