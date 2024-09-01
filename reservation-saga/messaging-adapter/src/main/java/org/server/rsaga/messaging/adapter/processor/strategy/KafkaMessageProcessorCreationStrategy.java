package org.server.rsaga.messaging.adapter.processor.strategy;

import java.util.Properties;

public interface KafkaMessageProcessorCreationStrategy {
    void checkConfig(Properties config);
}