package org.server.reservation.saga.message;

public interface SagaMessage<T> {
    Key getCorrelationId();
    int getStepId();
    T getPayload();
}