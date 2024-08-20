package org.server.rsaga.messaging.adapter.processor.factory;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.server.rsaga.messaging.adapter.processor.KafkaMessageProcessor;
import org.server.rsaga.messaging.adapter.processor.strategy.KafkaMessageProcessorCreationStrategy;
import org.server.rsaga.messaging.adapter.processor.strategy.KafkaMessageProcessorType;
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
    private final List<KafkaMessageProcessorCreationStrategy> processorCreationStrategies;
    private final Map<KafkaMessageProcessorType, KafkaMessageProcessorCreationStrategy> processorCreationStrategyMap =
            new EnumMap<>(KafkaMessageProcessorType.class);

    @PostConstruct
    private void init() {
        for (KafkaMessageProcessorCreationStrategy processorCreationStrategy : processorCreationStrategies) {
            processorCreationStrategyMap.put(processorCreationStrategy.getType(), processorCreationStrategy);
        }
    }

    @Override
    public <K, V> KafkaMessageProcessor<K, V> create(Properties config,
                                                     UnaryOperator<Message<K, V>> operator,
                                                     KafkaMessageProcessorType processorType
    ) {
        KafkaMessageProcessorCreationStrategy creationStrategy = processorCreationStrategyMap.get(processorType);
        // KafkaMessageProcessor 생성 및 반환
        return creationStrategy.createMessageProcessor(config, operator);
    }
}