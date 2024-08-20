package org.server.rsaga.messaging.adapter.consumer.strategy;

import org.server.rsaga.messaging.adapter.consumer.KafkaMessageConsumer;

import java.util.Properties;

public interface KafkaMessageConsumerCreationStrategy {
    <K, V> KafkaMessageConsumer<K, V> createConsumer(Properties config);
    KafkaMessageConsumerType getType();
}
