package org.server.rsaga.saga.api;

import org.server.rsaga.saga.message.SagaMessage;

public interface SagaCoordinator<T> {
    void start(SagaMessage<T> initSagaMessage);

    void handleMessage(SagaMessage<T> sagaMessage);
}