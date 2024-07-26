package org.server.rsaga.saga.state.factory;

import org.server.rsaga.saga.message.SagaMessage;
import org.server.rsaga.saga.promise.SagaPromise;
import org.server.rsaga.saga.state.SagaState;
import org.server.rsaga.saga.state.impl.InMemorySagaState;

public class InMemorySagaStateFactory<T> implements SagaStateFactory<T> {

    @Override
    public SagaState<T> create(SagaPromise<?, SagaMessage<T>>[] sagaPromises) {
        return new InMemorySagaState<>(sagaPromises);
    }
}