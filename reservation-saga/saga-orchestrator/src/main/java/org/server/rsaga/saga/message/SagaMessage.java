package org.server.rsaga.saga.message;

public interface SagaMessage<T> {
    Key getCorrelationId();
    int getStepId();
    T getPayload();
}