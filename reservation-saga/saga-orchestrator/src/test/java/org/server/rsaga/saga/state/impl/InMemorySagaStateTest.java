package org.server.rsaga.saga.state.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.server.rsaga.messaging.message.Message;
import org.server.rsaga.saga.api.SagaMessage;
import org.server.rsaga.saga.promise.SagaPromise;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

/**
 * <pre>
 * 사가의 각 실행 상태를 관리하는 클래스
 * </pre>
 */
@DisplayName("SagaState Implementation Tests")
@ExtendWith(MockitoExtension.class)
class InMemorySagaStateTest {
    @Mock
    SagaPromise<?, SagaMessage<Object, Object>> sagaPromise1;
    @Mock
    SagaPromise<?, SagaMessage<Object, Object>> sagaPromise2;
    @Mock
    SagaPromise<?, SagaMessage<Object, Object>> sagaPromise3;

    @Mock
    SagaMessage<Object, Object> sagaMessage;

    InMemorySagaState<Object, Object> inMemorySagaState;

    @BeforeEach
    void setUp() {
        SagaPromise<?, SagaMessage<Object, Object>>[] sagaPromises = new SagaPromise[]{sagaPromise1, sagaPromise2, sagaPromise3};
        inMemorySagaState = new InMemorySagaState<>(sagaPromises);
    }

    @Test
    @DisplayName("SagaMessage - 특정 SagaPromise 의 setSuccess 로 응답 설정 - 성공")
    void should_setSuccess_when_messageIsGiven() {
        // given
        when(sagaMessage.stepId()).thenReturn(0);
        when(sagaMessage.status()).thenReturn(Message.Status.RESPONSE_SUCCESS);

        // when
        inMemorySagaState.updateState(sagaMessage);

        // then
        verify(sagaPromise1, only().description("The success method should be called with the message")).success(sagaMessage);
        verify(sagaPromise2, never().description("The success method should not be called on other saga promises")).success(any());
        verify(sagaPromise3, never().description("The success method should not be called on other saga promises")).success(any());
    }

    @Test
    @DisplayName("SagaMessage - setSuccess 후 예외 발생 - failure 설정")
    void should_setFailure_when_exceptionOccurs() {
        // given
        when(sagaMessage.stepId()).thenReturn(0);
        when(sagaMessage.status()).thenReturn(Message.Status.RESPONSE_SUCCESS);
        doThrow(new RuntimeException()).when(sagaPromise1).success(sagaMessage);

        // when
        inMemorySagaState.updateState(sagaMessage);

        // then
        verify(sagaPromise1, times(1).description("The success method should be called with the message"))
                .success(sagaMessage);
        verify(sagaPromise1, times(1).description("The failure method should be called on exception"))
                .failure(any(SagaMessage.class), any(RuntimeException.class));
        verify(sagaPromise2, only().description("The failure method should be called on all saga promises"))
                .cancelDueToOtherFailure(any(RuntimeException.class));
        verify(sagaPromise3, only().description("The failure method should be called on all saga promises"))
                .cancelDueToOtherFailure(any(RuntimeException.class));
    }

    @Test
    @DisplayName("Throwable - 모두 setFailure 설정 - 모두 fail 성공")
    void should_setFailure_when_causeIsGiven() {
        // given
        Throwable cause = new Throwable("Request failed.");

        // when
        inMemorySagaState.handleException(cause);

        // then
        verify(sagaPromise1, only().description("The failure method should be called with the cause")).cancelDueToOtherFailure(cause);
        verify(sagaPromise2, only().description("The failure method should be called with the cause")).cancelDueToOtherFailure(cause);
        verify(sagaPromise3, only().description("The failure method should be called with the cause")).cancelDueToOtherFailure(cause);
    }

    @Test
    @DisplayName("SagaPromises - 모두 isDone() return true - state 의 isDone() return true")
    void should_returnTrue_when_allSagaPromisesAreDone() {
        // given
        when(sagaPromise1.isDone()).thenReturn(true);
        when(sagaPromise2.isDone()).thenReturn(true);
        when(sagaPromise3.isDone()).thenReturn(true);

        // when
        boolean result = inMemorySagaState.isAllDone();

        // then
        assertTrue(result, "isDone should return true when all saga promises are done");
    }

    @Test
    @DisplayName("SagaPromises - 특정 SagaPromise 아직 완료 안됨 - state 의 isDone() return false")
    void should_returnFalse_when_notAllSagaPromisesAreDone() {
        // given
        when(sagaPromise1.isDone()).thenReturn(true);
        when(sagaPromise2.isDone()).thenReturn(true);
        when(sagaPromise3.isDone()).thenReturn(false);

        // when
        boolean result = inMemorySagaState.isAllDone();

        // then
        assertFalse(result, "isDone() should return false when not all saga promises are done");
    }
}