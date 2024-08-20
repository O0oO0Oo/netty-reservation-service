package org.server.rsaga.messaging.adapter.consumer.strategy;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.server.rsaga.messaging.adapter.consumer.KafkaMessageConsumer;
import org.server.rsaga.messaging.adapter.consumer.impl.AtLeastOnceKafkaMessageConsumer;
import org.server.rsaga.messaging.adapter.consumer.util.KafkaConsumerPropertiesUtil;
import org.springframework.stereotype.Component;

import java.util.Properties;

@Component
@RequiredArgsConstructor
public class AtLeastOnceKafkaMessageConsumerCreationStrategy implements KafkaMessageConsumerCreationStrategy{
    private final KafkaConsumerPropertiesUtil propertiesUtil;

    @Override
    public <K, V> KafkaMessageConsumer<K, V> createConsumer(Properties config) {
        Properties properties = propertiesUtil.atLeastOnceProtobufConsumerProperties(config);

        String consumerTopic = (String) properties.remove("consumer.topic");

        KafkaConsumer<K, V> kafkaConsumer = new KafkaConsumer<>(properties);
        return new AtLeastOnceKafkaMessageConsumer<>(kafkaConsumer, consumerTopic);
    }

    @Override
    public KafkaMessageConsumerType getType() {
        return KafkaMessageConsumerType.AT_LEAST_ONCE;
    }
}