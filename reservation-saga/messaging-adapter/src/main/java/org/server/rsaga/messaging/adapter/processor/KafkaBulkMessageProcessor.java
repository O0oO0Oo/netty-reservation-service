package org.server.rsaga.messaging.adapter.processor;

import org.server.rsaga.messaging.message.Message;
import org.server.rsaga.messaging.processor.SameTypeBulkMessageProcessor;

import java.util.List;

public interface KafkaBulkMessageProcessor<K, V> extends SameTypeBulkMessageProcessor<K, V, List<Message<K, V>>> {
}
