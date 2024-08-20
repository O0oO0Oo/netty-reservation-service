package org.server.rsaga.messaging.consumer;

import org.server.rsaga.messaging.message.Message;

import java.util.function.Consumer;

public interface MessageConsumer<K, V> {
    void registerHandler(Consumer<Message<K, V>> consumer);
}