package org.server.rsaga.saga.state.factory;

import org.server.rsaga.saga.api.SagaMessage;
import org.server.rsaga.saga.promise.SagaPromise;
import org.server.rsaga.saga.state.SagaState;
import org.server.rsaga.saga.state.impl.InMemorySagaState;

public class InMemorySagaStateFactory<K, V> implements SagaStateFactory<K, V> {

    @Override
    public SagaState<K, V> create(SagaPromise<?, SagaMessage<K, V>>[] sagaPromises) {
        return new InMemorySagaState<>(sagaPromises);
    }
}