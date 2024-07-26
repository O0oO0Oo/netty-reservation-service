package org.server.rsaga.saga.state.impl;

import lombok.extern.slf4j.Slf4j;
import org.server.rsaga.saga.message.Key;
import org.server.rsaga.saga.message.SagaMessage;
import org.server.rsaga.saga.promise.SagaPromise;
import org.server.rsaga.saga.state.SagaState;
import org.server.rsaga.saga.state.SagaStateCache;
import org.server.rsaga.saga.state.SagaStateManager;
import org.server.rsaga.saga.state.factory.SagaStateFactory;

@Slf4j
public class InMemorySagaStateManager<T> implements SagaStateManager<T> {
    private final SagaStateFactory<T> sagaStateFactory;
    private final SagaStateCache<T> cache;

    public InMemorySagaStateManager(SagaStateFactory<T> sagaStateFactory, SagaStateCache<T> cache) {
        this.sagaStateFactory = sagaStateFactory;
        this.cache = cache;
    }

    @Override
    public void initializeState(SagaMessage<T> initSagaMessage, SagaPromise<?, SagaMessage<T>>[] sagaPromises) {
        // id 중복
        SagaState<T> sagaState = sagaStateFactory.create(sagaPromises);
        sagaState.setSuccess(initSagaMessage);
    }

    @Override
    public void update(SagaMessage<T> sagaMessage) {
        Key correlationId = sagaMessage.getCorrelationId();
        SagaState<T> state = cache.get(correlationId);
        if (state != null) {
            state.setSuccess(sagaMessage);
            removeSuccessCache(correlationId, state);
        }
    }

    private void removeSuccessCache(Key id, SagaState<T> state) {
        if (state.isDone()) {
            log.info("Correlation id : {} is succeed.", id);
            cache.remove(id);
        }
    }
}