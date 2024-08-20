package org.server.rsaga.saga.state.factory;

import org.server.rsaga.saga.state.SagaStateManager;

public interface SagaStateComponentFactory<K, V> {
    SagaStateManager<K, V> createSagaStateManger();
    SagaStateFactory<K, V> createSagaStateFactory();
}