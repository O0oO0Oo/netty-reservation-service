package org.server.rsaga.messaging.processor;

import org.server.rsaga.messaging.message.Message;

import java.util.function.UnaryOperator;

public interface SameTypeMessageProcessor<K, V> {
    void registerHandler(UnaryOperator<Message<K, V>> function);
}