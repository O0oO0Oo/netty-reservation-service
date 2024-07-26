package org.server.rsaga.saga.state.factory;

import org.server.rsaga.saga.state.SagaStateManager;

public interface SagaStateComponentFactory<T> {
    SagaStateManager<T> createSagaStateManger();
    SagaStateFactory<T> createSagaStateFactory();
}