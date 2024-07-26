package org.server.rsaga.saga.step;

import org.server.rsaga.saga.message.SagaMessage;

public interface SingleEventSagaStep<I> extends SagaStep {
    void publishEvent(SagaMessage<I> input);
}
