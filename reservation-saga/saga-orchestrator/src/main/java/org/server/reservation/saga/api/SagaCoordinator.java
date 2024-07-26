package org.server.reservation.saga.api;

import org.server.reservation.saga.message.SagaMessage;

public interface SagaCoordinator<T> {
    void start(SagaMessage<T> initSagaMessage);

    void handleMessage(SagaMessage<T> sagaMessage);
}