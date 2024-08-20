package org.server.rsaga.messaging.adapter.consumer.factory;

import io.confluent.kafka.serializers.protobuf.KafkaProtobufDeserializerConfig;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.server.rsaga.messaging.adapter.consumer.KafkaMessageConsumer;
import org.server.rsaga.messaging.adapter.consumer.strategy.KafkaMessageConsumerCreationStrategy;
import org.server.rsaga.messaging.adapter.consumer.strategy.KafkaMessageConsumerType;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

@Component
@RequiredArgsConstructor
public class KafkaMessageConsumerFactoryImpl implements KafkaMessageConsumerFactory {
    private final List<KafkaMessageConsumerCreationStrategy> consumerCreationStrategies;
    private final Map<KafkaMessageConsumerType, KafkaMessageConsumerCreationStrategy> consumerCreationStrategyMap
            = new EnumMap<>(KafkaMessageConsumerType.class);

    @PostConstruct
    private void init() {
        for (KafkaMessageConsumerCreationStrategy consumerCreationStrategy : consumerCreationStrategies) {
            consumerCreationStrategyMap.put(consumerCreationStrategy.getType(), consumerCreationStrategy);
        }
    }

    @Override
    public <K, V> KafkaMessageConsumer<K, V> createConsumer(Properties config, KafkaMessageConsumerType consumerType) {
        validateConfig(config);

        KafkaMessageConsumerCreationStrategy consumerCreationStrategy = consumerCreationStrategyMap.get(consumerType);
        return consumerCreationStrategy.createConsumer(config);
    }

    private void validateConfig(Properties config) {
        Object groupId = config.get(ConsumerConfig.GROUP_ID_CONFIG);
        Object topic = config.get("consumer.topic");
        Object specificValueType = config.get(KafkaProtobufDeserializerConfig.SPECIFIC_PROTOBUF_VALUE_TYPE);

        if (groupId == null) {
            throw new IllegalArgumentException("group.id cannot be null");
        }

        if (topic == null) {
            throw new IllegalArgumentException("consumer.topic cannot be null");
        }

        if (specificValueType == null) {
            throw new IllegalArgumentException("specific.protobuf.value.type cannot be null");
        }
    }
}
