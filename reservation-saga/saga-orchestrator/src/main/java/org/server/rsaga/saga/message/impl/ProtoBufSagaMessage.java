package org.server.rsaga.saga.message.impl;

import org.server.rsaga.saga.message.Key;
import org.server.rsaga.saga.message.SagaMessage;

public final class ProtoBufSagaMessage<T> implements SagaMessage<T> {
    private final Key correlationId;
    private final int stepId;
    private final T payload;

    public ProtoBufSagaMessage(Key correlationId, int stepId, T payload) {
        this.correlationId = correlationId;
        this.stepId = stepId;
        this.payload = payload;
    }


    @Override
    public Key getCorrelationId() {
        return this.correlationId;
    }

    @Override
    public int getStepId() {
        return this.stepId;
    }

    @Override
    public T getPayload() {
        return this.payload;
    }
}