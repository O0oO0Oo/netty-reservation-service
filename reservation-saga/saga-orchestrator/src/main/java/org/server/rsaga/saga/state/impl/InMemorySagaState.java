package org.server.rsaga.saga.state.impl;

import org.server.rsaga.messaging.message.Message;
import org.server.rsaga.saga.api.SagaMessage;
import org.server.rsaga.saga.exception.RemoteServiceException;
import org.server.rsaga.saga.promise.SagaPromise;
import org.server.rsaga.saga.state.SagaState;

import java.util.Arrays;

public class InMemorySagaState<K, V> implements SagaState<K, V> {
    private final SagaPromise<?, SagaMessage<K, V>>[] sagaPromises;
    public InMemorySagaState(SagaPromise<?, SagaMessage<K, V>>[] sagaPromises) {
        this.sagaPromises = sagaPromises;
    }

    @Override
    public void updateState(SagaMessage<K, V> message) {
        if (message.status().equals(Message.Status.RESPONSE_FAILED)) {
            setFailure(
                   message, new RemoteServiceException(message.errorCode(), message.errorMessage())
            );
        }
        else {
            setSuccess(message);
        }
    }

    /**
     * 성공 요청 실패 시 setFailure 설정
     * @param message
     */
    private void setSuccess(SagaMessage<K, V> message) {
        try {
            sagaPromises[message.stepId()].success(message);
        } catch (Exception e) {
            setFailure(message, e);
        }
    }

    /**
     * 모든 작업 실패로 설정, Compensate 요청 시작
     * @param cause
     */
    private void setFailure(SagaMessage<K, V> message, Throwable cause) {
        int stepId = message.stepId();
        sagaPromises[stepId].failure(message, cause);

        for (int i = 0; i < sagaPromises.length; i++) {
            if (i != stepId) {
                sagaPromises[i].cancelDueToOtherFailure(cause);
            }
        }
    }

    @Override
    public void handleException(Throwable cause) {
        for (SagaPromise<?, SagaMessage<K, V>> sagaPromise : sagaPromises) {
            sagaPromise.cancelDueToOtherFailure(cause);
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
    public boolean isAllDone() {
        return Arrays.stream(sagaPromises).allMatch(SagaPromise::isDone);
    }
}