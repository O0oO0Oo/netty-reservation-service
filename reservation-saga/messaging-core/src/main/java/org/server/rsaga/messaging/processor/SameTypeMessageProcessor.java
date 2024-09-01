package org.server.rsaga.messaging.processor;

import java.util.function.UnaryOperator;

public interface SameTypeMessageProcessor<T> {
    void registerHandler(UnaryOperator<T> function);
}