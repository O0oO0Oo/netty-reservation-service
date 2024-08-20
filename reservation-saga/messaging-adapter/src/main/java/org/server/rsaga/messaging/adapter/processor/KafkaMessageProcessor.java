package org.server.rsaga.messaging.adapter.processor;

import org.server.rsaga.messaging.processor.SameTypeMessageProcessor;

public interface KafkaMessageProcessor<K, V> extends SameTypeMessageProcessor<K, V> {
}