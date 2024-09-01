package org.server.rsaga.saga.state.impl;

import io.hypersistence.tsid.TSID;
import lombok.extern.slf4j.Slf4j;
import org.server.rsaga.saga.api.SagaMessage;
import org.server.rsaga.saga.promise.SagaPromise;
import org.server.rsaga.saga.state.SagaState;
import org.server.rsaga.saga.state.SagaStateCache;
import org.server.rsaga.saga.state.SagaStateManager;
import org.server.rsaga.saga.state.factory.SagaStateFactory;

@Slf4j
public class InMemorySagaStateManager<K, V> implements SagaStateManager<K, V> {
    private final SagaStateFactory<K, V> sagaStateFactory;
    private final SagaStateCache<K, V> cache;

    public InMemorySagaStateManager(SagaStateFactory<K, V> sagaStateFactory, SagaStateCache<K, V> cache) {
        this.sagaStateFactory = sagaStateFactory;
        this.cache = cache;
    }

    @Override
    public void initializeState(SagaMessage<K, V> initSagaMessage, SagaPromise<?, SagaMessage<K, V>>[] sagaPromises) {
        // id 중복
        SagaState<K, V> sagaState = sagaStateFactory.create(sagaPromises);
        sagaState.updateState(initSagaMessage);

        cache.put(initSagaMessage.correlationId(), sagaState);
    }

    @Override
    public void update(SagaMessage<K, V> sagaMessage) {
        TSID correlationId = sagaMessage.correlationId();
        SagaState<K, V> state = cache.get(correlationId);

        if (state != null) {
            state.updateState(sagaMessage);
            removeSuccessCache(correlationId, state);
        }
    }

    private void removeSuccessCache(TSID id, SagaState<K, V> state) {
        if (state.isAllDone()) {
            log.debug("Correlation ID: {} succeeded.", id);
            cache.remove(id);
        }
    }
}