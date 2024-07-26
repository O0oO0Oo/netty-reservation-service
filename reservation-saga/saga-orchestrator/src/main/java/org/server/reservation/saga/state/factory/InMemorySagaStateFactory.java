package org.server.reservation.saga.state.factory;

import org.server.reservation.saga.message.SagaMessage;
import org.server.reservation.saga.promise.SagaPromise;
import org.server.reservation.saga.state.SagaState;
import org.server.reservation.saga.state.impl.InMemorySagaState;

public class InMemorySagaStateFactory<T> implements SagaStateFactory<T> {

    @Override
    public SagaState<T> create(SagaPromise<?, SagaMessage<T>>[] sagaPromises) {
        return new InMemorySagaState<>(sagaPromises);
    }
}