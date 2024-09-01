package org.server.rsaga.messaging.adapter.processor.factory;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.server.rsaga.messaging.adapter.processor.KafkaBulkMessageProcessor;
import org.server.rsaga.messaging.adapter.processor.KafkaSingleMessageProcessor;
import org.server.rsaga.messaging.adapter.processor.strategy.KafkaBulkMessageProcessorCreationStrategy;
import org.server.rsaga.messaging.adapter.processor.strategy.KafkaBulkMessageProcessorType;
import org.server.rsaga.messaging.adapter.processor.strategy.KafkaSingleMessageProcessorCreationStrategy;
import org.server.rsaga.messaging.adapter.processor.strategy.KafkaSingleMessageProcessorType;
import org.server.rsaga.messaging.message.Message;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.UnaryOperator;

@Component
@RequiredArgsConstructor
public class KafkaMessageProcessorFactoryImpl implements KafkaMessageProcessorFactory {
    private final List<KafkaSingleMessageProcessorCreationStrategy> singleMessageProcessorCreationStrategies;
    private final Map<KafkaSingleMessageProcessorType, KafkaSingleMessageProcessorCreationStrategy> singleMessageProcessorCreationStrategyMap =
            new EnumMap<>(KafkaSingleMessageProcessorType.class);

    private final List<KafkaBulkMessageProcessorCreationStrategy> bulkMessageProcessorCreationStrategies;
    private final Map<KafkaBulkMessageProcessorType, KafkaBulkMessageProcessorCreationStrategy> bulkMessageProcessorCreationStrategyMap =
            new EnumMap<>(KafkaBulkMessageProcessorType.class);

    @PostConstruct
    private void init() {
        for (KafkaSingleMessageProcessorCreationStrategy processorCreationStrategy : singleMessageProcessorCreationStrategies) {
            singleMessageProcessorCreationStrategyMap.put(processorCreationStrategy.getType(), processorCreationStrategy);
        }

        for (KafkaBulkMessageProcessorCreationStrategy processorCreationStrategy : bulkMessageProcessorCreationStrategies) {
            bulkMessageProcessorCreationStrategyMap.put(processorCreationStrategy.getType(), processorCreationStrategy);
        }
    }

    @Override
    public <K, V> KafkaSingleMessageProcessor<K, V> create(Properties config,
                                                           UnaryOperator<Message<K, V>> operator,
                                                           KafkaSingleMessageProcessorType processorType
    ) {
        KafkaSingleMessageProcessorCreationStrategy creationStrategy = singleMessageProcessorCreationStrategyMap.get(processorType);
        // KafkaMessageProcessor 생성 및 반환
        return creationStrategy.createMessageProcessor(config, operator);
    }

    @Override
    public <K, V> KafkaBulkMessageProcessor<K, V> create(Properties config, UnaryOperator<List<Message<K, V>>> operator, KafkaBulkMessageProcessorType processorType) {
        KafkaBulkMessageProcessorCreationStrategy creationStrategy = bulkMessageProcessorCreationStrategyMap.get(processorType);
        return creationStrategy.createMessageProcessor(config, operator);
    }
}