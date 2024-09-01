package org.server.rsaga.messaging.adapter.processor;

import org.server.rsaga.messaging.message.Message;
import org.server.rsaga.messaging.processor.SameTypeSingleMessageProcessor;

public interface KafkaSingleMessageProcessor<K, V> extends SameTypeSingleMessageProcessor<K, V, Message<K, V>> {
}