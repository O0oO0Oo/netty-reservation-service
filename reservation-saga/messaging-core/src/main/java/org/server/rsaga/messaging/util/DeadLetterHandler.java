package org.server.rsaga.messaging.util;

import org.server.rsaga.messaging.message.Message;

import java.util.List;

public interface DeadLetterHandler<K, V> {
    void handle(Message<K, V> message, Throwable e);
    void handle(List<Message<K, V>> messages, Throwable throwable);
}