package org.server.rsaga.saga.state;

import org.server.rsaga.saga.message.SagaMessage;
import org.server.rsaga.saga.promise.SagaPromise;

/**
 * Saga 의 상태 관리 클래스
 * @param <T>
 */
public interface SagaStateManager<T> {
    void initializeState(SagaMessage<T> initSagaMessage, SagaPromise<?, SagaMessage<T>>[] executionContext);
    void update(SagaMessage<T> sagaMessage);
}