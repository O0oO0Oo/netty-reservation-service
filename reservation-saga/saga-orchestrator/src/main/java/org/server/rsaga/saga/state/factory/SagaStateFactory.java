package org.server.rsaga.saga.state.factory;

import org.server.rsaga.saga.message.SagaMessage;
import org.server.rsaga.saga.promise.SagaPromise;
import org.server.rsaga.saga.state.SagaState;

public interface SagaStateFactory<T> {
    SagaState<T> create(SagaPromise<?, SagaMessage<T>>[] sagaPromises);
}
