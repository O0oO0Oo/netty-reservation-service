package org.server.rsaga.messaging.adapter.processor.strategy;

import org.server.rsaga.messaging.adapter.processor.KafkaMessageProcessor;
import org.server.rsaga.messaging.message.Message;

import java.util.Properties;
import java.util.function.UnaryOperator;

public interface KafkaMessageProcessorCreationStrategy {
    <K, V> KafkaMessageProcessor<K, V> createMessageProcessor(Properties config,
                                                              UnaryOperator<Message<K, V>> operator);

    void checkConfig(Properties config);
    KafkaMessageProcessorType getType();
}
