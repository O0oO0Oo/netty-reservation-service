package org.server.reservation.saga.state;

import org.server.reservation.saga.message.SagaMessage;
import org.server.reservation.saga.promise.SagaPromise;

/**
 * Saga 의 상태 관리 클래스
 * @param <T>
 */
public interface SagaStateManager<T> {
    void initializeState(SagaMessage<T> initSagaMessage, SagaPromise<?, SagaMessage<T>>[] executionContext);
    void update(SagaMessage<T> sagaMessage);
}