package org.server.rsaga.saga.state.factory;

import org.server.rsaga.saga.state.SagaStateManager;
import org.server.rsaga.saga.state.impl.InMemoryGuavaSagaStateCache;
import org.server.rsaga.saga.state.impl.InMemorySagaStateManager;

public class InMemorySagaStateComponentFactory<K, V> implements SagaStateComponentFactory<K, V> {
    @Override
    public SagaStateManager<K, V> createSagaStateManger() {
        return new InMemorySagaStateManager<>(createSagaStateFactory(), new InMemoryGuavaSagaStateCache<>(20000, 5));
    }

    @Override
    public SagaStateFactory<K, V> createSagaStateFactory() {
        return new InMemorySagaStateFactory<>();
    }
}