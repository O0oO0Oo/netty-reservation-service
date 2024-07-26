package org.server.reservation.saga.api.impl;

import lombok.extern.slf4j.Slf4j;
import org.server.reservation.saga.message.MessageConsumer;
import org.server.reservation.saga.message.SagaMessage;
import org.server.reservation.saga.api.SagaCoordinator;
import org.server.reservation.saga.api.SagaDefinition;
import org.server.reservation.saga.promise.SagaPromise;
import org.server.reservation.saga.state.SagaStateManager;

@Slf4j
public class DefaultSagaCoordinator<T> implements SagaCoordinator<T> {
    private final SagaDefinition<T> sagaDefinition;
    private final SagaStateManager<T> sagaStateManager;

    public DefaultSagaCoordinator(SagaDefinition<T> sagaDefinition, SagaStateManager<T> sagaStateManager, MessageConsumer<T> messageConsumer) {
        this.sagaDefinition = sagaDefinition;
        this.sagaStateManager = sagaStateManager;
        messageConsumer.registerHandler(this::handleMessage);
    }

    @Override
    public void start(SagaMessage<T> initSagaMessage) {
        SagaPromise<?, SagaMessage<T>>[] sagaPromises = sagaDefinition.initializeSaga();
        sagaStateManager.initializeState(initSagaMessage, sagaPromises);
    }

    @Override
    public void handleMessage(SagaMessage<T> sagaMessage) {
        sagaStateManager.update(sagaMessage);
    }
}