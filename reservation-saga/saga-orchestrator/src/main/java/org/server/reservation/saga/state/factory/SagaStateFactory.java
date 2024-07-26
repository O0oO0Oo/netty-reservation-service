package org.server.reservation.saga.state.factory;

import org.server.reservation.saga.message.SagaMessage;
import org.server.reservation.saga.promise.SagaPromise;
import org.server.reservation.saga.state.SagaState;

public interface SagaStateFactory<T> {
    SagaState<T> create(SagaPromise<?, SagaMessage<T>>[] sagaPromises);
}
