package org.server.rsaga.messaging.adapter.producer.strategy;

import lombok.RequiredArgsConstructor;
import org.server.rsaga.messaging.adapter.producer.KafkaMessageProducer;
import org.server.rsaga.messaging.adapter.producer.impl.AtLeastOnceKafkaMessageProducer;
import org.server.rsaga.messaging.adapter.producer.util.KafkaProducerPropertiesUtil;
import org.springframework.stereotype.Component;

import java.util.Properties;

@Component
@RequiredArgsConstructor
public class AtLeastOnceKafkaMessageProducerCreationStrategy implements KafkaMessageProducerCreationStrategy {
    private final KafkaProducerPropertiesUtil propertiesUtil;

    @Override
    public <K, V> KafkaMessageProducer<K, V> createProducer(Properties config) {
        Properties properties = propertiesUtil.atLeastOnceProtobufProducerProps(config);
        return new AtLeastOnceKafkaMessageProducer<>(properties);
    }

    @Override
    public void checkConfig(Properties config) {

    }

    @Override
    public KafkaMessageProducerType getType() {
        return KafkaMessageProducerType.AT_LEAST_ONCE;
    }
}
