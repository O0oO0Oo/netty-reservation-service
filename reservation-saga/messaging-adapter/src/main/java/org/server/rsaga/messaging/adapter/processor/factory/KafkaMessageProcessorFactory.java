package org.server.rsaga.messaging.adapter.processor.factory;

import org.server.rsaga.messaging.adapter.processor.KafkaMessageProcessor;
import org.server.rsaga.messaging.adapter.processor.strategy.KafkaMessageProcessorType;
import org.server.rsaga.messaging.message.Message;

import java.util.Properties;
import java.util.function.UnaryOperator;

public interface KafkaMessageProcessorFactory {
    <K, V> KafkaMessageProcessor<K, V> create(Properties config,
                                              UnaryOperator<Message<K, V>> operator,
                                              KafkaMessageProcessorType processorType);
}
