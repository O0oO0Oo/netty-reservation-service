package org.server.reservation.saga.state.impl;

import org.server.reservation.saga.message.SagaMessage;
import org.server.reservation.saga.promise.SagaPromise;
import org.server.reservation.saga.state.SagaState;

import java.util.Arrays;

public class InMemorySagaState<T> implements SagaState<T> {
    private final SagaPromise<?, SagaMessage<T>>[] sagaPromises;
    public InMemorySagaState(SagaPromise<?, SagaMessage<T>>[] sagaPromises) {
        this.sagaPromises = sagaPromises;
    }

    /**
     * 성공 요청 실패 시 setFailure 설정
     * @param message
     */
    @Override
    public void setSuccess(SagaMessage<T> message) {
        try {
            sagaPromises[message.getStepId()].success(message);
        } catch (Exception e) {
            setFailure(e);
        }
    }

    /**
     * 모든 작업 실패로 설정, Compensate 요청 시작
     * @param cause
     */
    @Override
    public void setFailure(Throwable cause) {
        for (SagaPromise<?, SagaMessage<T>> sagaPromise : sagaPromises) {
            sagaPromise.failure(cause);
        }
    }

    /**
     * <pre>
     * 1. 모든 SagaPromise 가 완료되었는지
     * 2. Compensate 동작 중이라면 완료되었는지
     * </pre>
     * @return 실패, 성공과 상관 없이, 완료시 true
     */
    @Override
    public boolean isDone() {
        return Arrays.stream(sagaPromises).allMatch(SagaPromise::isDone);
    }
}