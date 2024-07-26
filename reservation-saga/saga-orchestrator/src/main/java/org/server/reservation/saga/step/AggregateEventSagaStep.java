package org.server.reservation.saga.step;

import org.server.reservation.saga.message.SagaMessage;

import java.util.List;

public interface AggregateEventSagaStep<I> extends SagaStep{
    void publishEvent(List<SagaMessage<I>> input);
}
