package org.server.rsaga.messaging.adapter.producer.factory;

import org.server.rsaga.messaging.adapter.producer.KafkaMessageProducer;
import org.server.rsaga.messaging.adapter.producer.strategy.KafkaMessageProducerType;

import java.util.Properties;

public interface KafkaMessageProducerFactory {
    <K, V> KafkaMessageProducer<K, V> createProducer(Properties config, KafkaMessageProducerType producerType);
}