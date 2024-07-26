package org.server.reservation.saga.message;

import java.util.function.Consumer;

public interface MessageConsumer<T> {
    void registerHandler(Consumer<SagaMessage<T>> consumer);
}