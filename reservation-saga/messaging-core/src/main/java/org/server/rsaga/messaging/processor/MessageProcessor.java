package org.server.rsaga.messaging.processor;

import org.server.rsaga.messaging.message.Message;

import java.util.function.Function;

public interface MessageProcessor<K1, V1, K2, V2> {
    void registerHandler(Function<Message<K1, V1>, Message<K2, V2>> function);
}