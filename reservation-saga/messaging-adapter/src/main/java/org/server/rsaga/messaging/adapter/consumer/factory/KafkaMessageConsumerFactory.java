package org.server.rsaga.messaging.adapter.consumer.factory;

import org.server.rsaga.messaging.adapter.consumer.KafkaMessageConsumer;
import org.server.rsaga.messaging.adapter.consumer.strategy.KafkaMessageConsumerType;

import java.util.Map;
import java.util.Properties;

public interface KafkaMessageConsumerFactory{
    /**
     * @param config {@link Map}{@code <String, String>} 으로 groupId, topic 을 포함해야 한다.
     * @return {@link KafkaMessageConsumer}
     */
    <K, V> KafkaMessageConsumer<K, V> createConsumer(Properties config, KafkaMessageConsumerType consumerType);
}