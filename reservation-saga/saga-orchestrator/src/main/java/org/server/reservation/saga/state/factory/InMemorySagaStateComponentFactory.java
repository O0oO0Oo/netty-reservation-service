package org.server.reservation.saga.state.factory;

import org.server.reservation.saga.state.SagaStateManager;
import org.server.reservation.saga.state.impl.InMemoryGuavaSagaStateCache;
import org.server.reservation.saga.state.impl.InMemorySagaStateManager;

public class InMemorySagaStateComponentFactory<T> implements SagaStateComponentFactory<T> {
    @Override
    public SagaStateManager<T> createSagaStateManger() {
        return new InMemorySagaStateManager<>(createSagaStateFactory(), new InMemoryGuavaSagaStateCache<>(20000, 5));
    }

    @Override
    public SagaStateFactory<T> createSagaStateFactory() {
        return new InMemorySagaStateFactory<>();
    }
}