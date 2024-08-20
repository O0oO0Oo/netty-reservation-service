package org.server.rsaga.saga.step;

import org.server.rsaga.saga.api.SagaMessage;

public interface SingleEventSagaStep<K, V> extends SagaStep {
    void publishEvent(SagaMessage<K, V> input);
}
