package org.server.rsaga.messaging.processor;

import org.server.rsaga.messaging.message.Message;

import java.util.List;

public interface SameTypeBulkMessageProcessor<K, V, T extends List<Message<K, V>>> extends SameTypeMessageProcessor<T>{
}