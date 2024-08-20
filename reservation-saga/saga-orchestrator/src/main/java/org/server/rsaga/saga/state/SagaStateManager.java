package org.server.rsaga.saga.state;

import org.server.rsaga.saga.api.SagaMessage;
import org.server.rsaga.saga.promise.SagaPromise;

/**
 * Saga 의 상태 관리 클래스
 * @param <K, V>
 */
public interface SagaStateManager<K, V> {
    void initializeState(SagaMessage<K, V> initSagaMessage, SagaPromise<?, SagaMessage<K, V>>[] executionContext);
    void update(SagaMessage<K, V> sagaMessage);
}