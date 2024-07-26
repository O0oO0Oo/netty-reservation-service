package org.server.rsaga.saga.step;

import org.server.rsaga.saga.message.SagaMessage;

import java.util.List;

public interface AggregateEventSagaStep<I> extends SagaStep{
    void publishEvent(List<SagaMessage<I>> input);
}
