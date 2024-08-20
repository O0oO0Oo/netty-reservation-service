package org.server.rsaga.messaging.adapter.producer.strategy;

import org.server.rsaga.messaging.adapter.producer.KafkaMessageProducer;

import java.util.Properties;

public interface KafkaMessageProducerCreationStrategy {
    <K, V> KafkaMessageProducer<K, V> createProducer(Properties config);
    void checkConfig(Properties config);
    KafkaMessageProducerType getType();
}