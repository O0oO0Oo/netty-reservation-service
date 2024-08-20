package org.server.rsaga.messaging.adapter.producer.factory;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.server.rsaga.messaging.adapter.producer.KafkaMessageProducer;
import org.server.rsaga.messaging.adapter.producer.strategy.KafkaMessageProducerCreationStrategy;
import org.server.rsaga.messaging.adapter.producer.strategy.KafkaMessageProducerType;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

@Component
@RequiredArgsConstructor
public class KafkaMessageProducerFactoryImpl implements KafkaMessageProducerFactory{
    private final List<KafkaMessageProducerCreationStrategy> producerCreationStrategies;
    private final Map<KafkaMessageProducerType, KafkaMessageProducerCreationStrategy> producerCreationStrategyMap
            = new EnumMap<>(KafkaMessageProducerType.class);

    @PostConstruct
    private void init() {
        for (KafkaMessageProducerCreationStrategy producerCreationStrategy : producerCreationStrategies) {
            producerCreationStrategyMap.put(producerCreationStrategy.getType(), producerCreationStrategy);
        }
    }

    @Override
    public <K, V> KafkaMessageProducer<K, V> createProducer(Properties config,
                                                            KafkaMessageProducerType producerType
    ) {
        KafkaMessageProducerCreationStrategy creationStrategy = producerCreationStrategyMap.get(producerType);
        return creationStrategy.createProducer(config);
    }
}