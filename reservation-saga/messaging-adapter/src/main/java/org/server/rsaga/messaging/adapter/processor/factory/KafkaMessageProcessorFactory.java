package org.server.rsaga.messaging.adapter.processor.factory;

import org.server.rsaga.messaging.adapter.processor.KafkaBulkMessageProcessor;
import org.server.rsaga.messaging.adapter.processor.KafkaSingleMessageProcessor;
import org.server.rsaga.messaging.adapter.processor.strategy.KafkaBulkMessageProcessorType;
import org.server.rsaga.messaging.adapter.processor.strategy.KafkaSingleMessageProcessorType;
import org.server.rsaga.messaging.message.Message;

import java.util.List;
import java.util.Properties;
import java.util.function.UnaryOperator;

public interface KafkaMessageProcessorFactory {
    <K, V> KafkaSingleMessageProcessor<K, V> create(Properties config,
                                                    UnaryOperator<Message<K, V>> operator,
                                                    KafkaSingleMessageProcessorType processorType);

    <K, V> KafkaBulkMessageProcessor<K, V> create(Properties config,
                                                  UnaryOperator<List<Message<K, V>>> operator,
                                                  KafkaBulkMessageProcessorType processorType);
}