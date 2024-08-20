package org.server.rsaga.messaging.util;

import org.server.rsaga.messaging.message.Message;

public interface DeadLetterHandler<K, V> {
    void handle(Message<K, V> message, Exception e);
}