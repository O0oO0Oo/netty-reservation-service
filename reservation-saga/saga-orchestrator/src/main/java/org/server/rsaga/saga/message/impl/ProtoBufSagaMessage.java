package org.server.rsaga.saga.message.impl;

import org.server.rsaga.saga.message.Key;
import org.server.rsaga.saga.message.SagaMessage;

public class ProtoBufSagaMessage<T extends com.google.protobuf.Message> implements SagaMessage<T> {
    private final Key correlationId;
    private final T payload;

    public ProtoBufSagaMessage(Key correlationId, T payload) {
        this.correlationId = correlationId;
        this.payload = payload;
    }

    @Override
    public Key getCorrelationId() {
        return this.correlationId;
    }

    @Override
    public int getStepId() {
        return 0;
    }

    @Override
    public T getPayload() {
        return this.payload;
    }
}