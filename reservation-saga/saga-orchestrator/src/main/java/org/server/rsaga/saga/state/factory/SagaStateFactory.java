package org.server.rsaga.saga.state.factory;

import org.server.rsaga.saga.api.SagaMessage;
import org.server.rsaga.saga.promise.SagaPromise;
import org.server.rsaga.saga.state.SagaState;

public interface SagaStateFactory<K, V> {
    SagaState<K, V> create(SagaPromise<?, SagaMessage<K, V>>[] sagaPromises);
}
