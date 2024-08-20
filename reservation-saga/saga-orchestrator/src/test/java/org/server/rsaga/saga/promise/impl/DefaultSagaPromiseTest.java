package org.server.rsaga.saga.promise.impl;

import io.netty.util.concurrent.Promise;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.server.rsaga.saga.api.SagaMessage;
import org.server.rsaga.saga.promise.SagaPromise;

import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@DisplayName("Default Transaction Request Logic Tests")
@ExtendWith(MockitoExtension.class)
class DefaultSagaPromiseTest {

    @Mock
    Promise<SagaMessage<?, ?>> promise;
    @Mock
    Consumer<Integer> executeOps;

    SagaPromise<Integer, SagaMessage<?, ?>> defaultSagaPromise;

    @BeforeEach
    void beforeEach() {
        defaultSagaPromise = new DefaultSagaPromise<>(promise, executeOps);
    }

    @Test
    @DisplayName("DefaultSagaPromise - 실행 -> 응답 - 성공")
    void should_success_when_executeAndReceiveResponse() {
        // given
        when(promise.isDone())
                .thenReturn(false, false, true);
        SagaMessage<?, ?> executionResponse = mock(SagaMessage.class);

        // when
        defaultSagaPromise.execute(10);
        defaultSagaPromise.execute(10);
        boolean isExecutionPending = defaultSagaPromise.isDone();

        defaultSagaPromise.success(executionResponse);
        defaultSagaPromise.success(executionResponse);
        boolean isExecutionSuccess = defaultSagaPromise.isDone();

        // then
        verify(executeOps, only().description("The request should be executed once."))
                .accept(anyInt());
        verify(promise, times(1).description("The setSuccess() method should be executed once."))
                .setSuccess(any(SagaMessage.class));
        assertFalse(isExecutionPending, "The isDone() method should return false.");
        assertTrue(isExecutionSuccess, "The isDone() method should return true.");
    }

    @Test
    @DisplayName("DefaultSagaPromise - 실행 -> 다른 서비스 실패 - 실패")
    void should_failure_when_executeAndOtherServiceFailed() {
        // given
        when(promise.isDone())
                .thenReturn(false, false, true);
        SagaMessage<?, ?> executionResponse = mock(SagaMessage.class);

        // when
        defaultSagaPromise.execute(10);
        defaultSagaPromise.execute(10);
        boolean isExecutionPending = defaultSagaPromise.isDone();

        defaultSagaPromise.cancelDueToOtherFailure(new RuntimeException());
        defaultSagaPromise.cancelDueToOtherFailure(new RuntimeException());
        boolean isExecutionSuccess = defaultSagaPromise.isDone();

        // then
        verify(executeOps, only().description("The request should be executed once."))
                .accept(anyInt());
        verify(promise, times(1).description("The setFailure() method should be executed once."))
                .setFailure(any(RuntimeException.class));
        assertFalse(isExecutionPending, "The isDone() method should return false.");
        assertTrue(isExecutionSuccess, "The isDone() method should return true.");
    }
}