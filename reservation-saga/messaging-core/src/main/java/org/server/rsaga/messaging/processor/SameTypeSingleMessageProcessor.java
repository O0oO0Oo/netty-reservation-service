package org.server.rsaga.messaging.processor;

import org.server.rsaga.messaging.message.Message;

public interface SameTypeSingleMessageProcessor<K, V, T extends Message<K, V>> extends SameTypeMessageProcessor<T>{
}