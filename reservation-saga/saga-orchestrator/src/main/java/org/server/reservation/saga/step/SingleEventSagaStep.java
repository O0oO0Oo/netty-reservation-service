package org.server.reservation.saga.step;

import org.server.reservation.saga.message.SagaMessage;

public interface SingleEventSagaStep<I> extends SagaStep {
    void publishEvent(SagaMessage<I> input);
}
