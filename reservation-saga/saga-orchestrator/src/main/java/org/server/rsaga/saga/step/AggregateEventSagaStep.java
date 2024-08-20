package org.server.rsaga.saga.step;

import org.server.rsaga.saga.api.SagaMessage;

import java.util.List;

public interface AggregateEventSagaStep<K, V> extends SagaStep{
    void publishEvent(List<SagaMessage<K, V>> input);
}
