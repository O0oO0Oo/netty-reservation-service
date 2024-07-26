package org.server.reservation.saga.state.factory;

import org.server.reservation.saga.state.SagaStateManager;

public interface SagaStateComponentFactory<T> {
    SagaStateManager<T> createSagaStateManger();
    SagaStateFactory<T> createSagaStateFactory();
}