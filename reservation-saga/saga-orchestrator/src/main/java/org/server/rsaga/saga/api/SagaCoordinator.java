package org.server.rsaga.saga.api;

public interface SagaCoordinator<K, V> {
    SagaMessage<K, V> start(SagaMessage<K, V> initSagaMessage);

    void handleMessage(SagaMessage<K, V> sagaMessage);
}