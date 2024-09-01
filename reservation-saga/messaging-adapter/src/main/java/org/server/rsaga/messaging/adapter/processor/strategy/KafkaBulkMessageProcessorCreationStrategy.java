package org.server.rsaga.messaging.adapter.processor.strategy;

import org.server.rsaga.messaging.adapter.processor.KafkaBulkMessageProcessor;
import org.server.rsaga.messaging.message.Message;

import java.util.List;
import java.util.Properties;
import java.util.function.UnaryOperator;

public interface KafkaBulkMessageProcessorCreationStrategy extends KafkaMessageProcessorCreationStrategy{
    <K, V> KafkaBulkMessageProcessor<K, V> createMessageProcessor(Properties config,
                                                                  UnaryOperator<List<Message<K, V>>> operator);
    KafkaBulkMessageProcessorType getType();
}