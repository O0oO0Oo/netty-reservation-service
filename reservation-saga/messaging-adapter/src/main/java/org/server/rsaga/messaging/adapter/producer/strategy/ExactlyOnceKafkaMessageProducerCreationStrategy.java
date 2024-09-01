package org.server.rsaga.messaging.adapter.producer.strategy;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.server.rsaga.messaging.adapter.producer.KafkaMessageProducer;
import org.server.rsaga.messaging.adapter.producer.impl.ExactlyOnceKafkaMessageProducer;
import org.server.rsaga.messaging.adapter.producer.util.KafkaProducerPropertiesUtil;
import org.springframework.stereotype.Component;

import java.util.Properties;

@Component
@RequiredArgsConstructor
public class ExactlyOnceKafkaMessageProducerCreationStrategy implements KafkaMessageProducerCreationStrategy {
    private final KafkaProducerPropertiesUtil propertiesUtil;

    @Override
    public <K, V> KafkaMessageProducer<K, V> createProducer(Properties config) {
        checkConfig(config);
        Properties properties = propertiesUtil.exactlyOnceProtobufProducerProps(config);
        return new ExactlyOnceKafkaMessageProducer<>(properties);
    }

    public void checkConfig(Properties config) {
        isContainsKey(config, ProducerConfig.TRANSACTIONAL_ID_CONFIG);
        config.putIfAbsent(ProducerConfig.RETRIES_CONFIG, "5");
    }

    private void isContainsKey(Properties config, String key) {
        if (!config.containsKey(key)) {
            throw new IllegalArgumentException("config must have a key of " + key);
        }
    }

    @Override
    public KafkaMessageProducerType getType() {
        return KafkaMessageProducerType.EXACTLY_ONCE;
    }
}