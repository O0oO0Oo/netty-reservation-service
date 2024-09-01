package org.server.rsaga.messaging.adapter.producer.strategy;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.server.rsaga.messaging.adapter.producer.KafkaMessageProducer;
import org.server.rsaga.messaging.adapter.producer.impl.AtLeastOnceKafkaMessageProducer;
import org.server.rsaga.messaging.adapter.producer.util.KafkaProducerPropertiesUtil;
import org.springframework.stereotype.Component;

import java.util.Properties;

@Component
@RequiredArgsConstructor
public class AtLeastOnceBatchKafkaMessageProducerCreationStrategy implements KafkaMessageProducerCreationStrategy{
    private final KafkaProducerPropertiesUtil propertiesUtil;

    @Override
    public <K, V> KafkaMessageProducer<K, V> createProducer(Properties config) {
        checkConfig(config);
        Properties properties = propertiesUtil.atLeastOnceProtobufProducerProps(config);
        return new AtLeastOnceKafkaMessageProducer<>(properties);
    }

    private void checkConfig(Properties config) {
        isContainsKeyOrElsePut(config, ProducerConfig.BATCH_SIZE_CONFIG, "1638400");
        isContainsKeyOrElsePut(config, ProducerConfig.LINGER_MS_CONFIG, "100");
    }

    private void isContainsKeyOrElsePut(Properties config, String key, String defaultValue) {
        config.putIfAbsent(key, defaultValue);
    }

    @Override
    public KafkaMessageProducerType getType() {
        return KafkaMessageProducerType.AT_LEAST_ONCE_BATCH;
    }
}
