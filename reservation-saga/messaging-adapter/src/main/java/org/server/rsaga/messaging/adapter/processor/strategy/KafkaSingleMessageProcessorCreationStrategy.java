package org.server.rsaga.messaging.adapter.processor.strategy;

import org.server.rsaga.messaging.adapter.processor.KafkaSingleMessageProcessor;
import org.server.rsaga.messaging.message.Message;

import java.util.Properties;
import java.util.function.UnaryOperator;

public interface KafkaSingleMessageProcessorCreationStrategy extends KafkaMessageProcessorCreationStrategy{
    <K, V> KafkaSingleMessageProcessor<K, V> createMessageProcessor(Properties config,
                                                                    UnaryOperator<Message<K, V>> operator);
    KafkaSingleMessageProcessorType getType();
}
