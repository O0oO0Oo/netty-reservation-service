package org.server.rsaga.saga.promise.impl;

import io.netty.util.concurrent.Promise;
import jakarta.persistence.criteria.CriteriaBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.server.rsaga.saga.api.SagaMessage;
import org.server.rsaga.saga.promise.SagaPromise;
import org.server.rsaga.saga.step.impl.StepType;

import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
/**
 * <pre>
 *  보상 요청의 동작
 *
 *   |
 *   v                       execute.accept()
 * [Compensable Promise] --> request -->  +---------+
 *                                        |Service A|
 * [Compensable Promise] <-- response <-- +---------+
 *   |                       setSuccess()
 *   v
 * [An error occurred]
 *   |
 *   |                       setFailure()
 *   v                       compensate.accept()
 * [Compensable Promise] --> request -->  +---------+
 *                                        |Service A|
 * [Compensable Promise] <-- response <-- +---------+
 *   |
 *   v
 *
 * </pre>
 */
@DisplayName("Compensable Transaction Request Logic Tests")
@ExtendWith(MockitoExtension.class)
class CompensableSagaPromiseTest {

    @Mock
    Promise<SagaMessage<?, ?>> promise;
    @Mock
    Consumer<Integer> executeOps;
    @Mock
    Consumer<Integer> compensateOps;

    SagaPromise<Integer, SagaMessage<?, ?>> compensableSagaPromise;

    @BeforeEach
    void beforeEach() {
        compensableSagaPromise = new CompensableSagaPromise<>(promise, executeOps, compensateOps);
    }

    @Test
    @DisplayName("CompensablePromise - 실행 -> 응답 - 성공")
    void should_success_when_executeAndReceiveResponse() {
        // given
        when(promise.isDone())
                .thenReturn(false, true);

        SagaMessage<?, ?> executionResponse = mock(SagaMessage.class);
        when(executionResponse.stepType()).thenReturn(StepType.EXECUTE);

        // when
        compensableSagaPromise.execute(10);
        compensableSagaPromise.success(executionResponse);

        boolean isExecutionSuccess = compensableSagaPromise.isDone();

        // then
        verify(executeOps, only().description("The request should be executed once."))
                .accept(anyInt());
        verify(promise, times(1).description("The success() method should be executed once."))
                .setSuccess(any(SagaMessage.class));
        verify(promise, never().description("The setFailure() method should never be executed."))
                .setFailure(any(Exception.class));
        verify(compensateOps, never().description("The compensation request should never be executed."))
                .accept(anyInt());
        assertTrue(isExecutionSuccess, "The isDone() method should return true.");
    }

    @Test
    @DisplayName("CompensableSagaPromise - 실행 -> 실행 응답 -> 다른 서비스의 실패 -> 보상 요청 -> 보상 응답 - 보상 성공")
    void should_isDoneTrue_when_executionAndFailure() {
        // given
        when(promise.isDone())
                .thenReturn(false, true);

        SagaMessage<?, ?> executionResponse = mock(SagaMessage.class);
        when(executionResponse.stepType()).thenReturn(StepType.EXECUTE);

        SagaMessage<?, ?> compensationResponse = mock(SagaMessage.class);
        when(compensationResponse.stepType()).thenReturn(StepType.COMPENSATE);

        when(promise.isSuccess()).thenReturn(true);
        // when
        compensableSagaPromise.execute(10);
        compensableSagaPromise.execute(10);

        compensableSagaPromise.success(executionResponse);
        compensableSagaPromise.success(executionResponse);

        compensableSagaPromise.cancelDueToOtherFailure(new IllegalStateException());
        compensableSagaPromise.cancelDueToOtherFailure(new IllegalStateException());

        compensableSagaPromise.success(compensationResponse);

        // then
        verify(executeOps, only().description("The request should be executed once."))
                .accept(anyInt());
        verify(promise, times(1).description("The success() method should be executed once."))
                .setSuccess(any(SagaMessage.class));
        verify(promise, never().description("The setFailure() method should never be executed."))
                .setFailure(any(Exception.class));
        verify(compensateOps, only().description("The compensation request should be executed once."))
                .accept(anyInt());
        assertTrue(compensableSagaPromise.isDone(), "The isDone() method should return true.");
    }

    @Test
    @DisplayName("CompensableSagaPromise - 실행 -> 응답 -> 다른 서비스의 실패 -> 보상 요청 -> 응답 안옴 - 보상 아직 안됨, isDone() false 반환")
    void should_isDoneFalse_when_executionAndFailure() {
        // given
        when(promise.isDone())
                .thenReturn(false, true);

        SagaMessage<?, ?> executionResponse = mock(SagaMessage.class);
        when(executionResponse.stepType()).thenReturn(StepType.EXECUTE);

        when(promise.isSuccess()).thenReturn(true);
        // when
        compensableSagaPromise.execute(10);
        compensableSagaPromise.execute(10);

        compensableSagaPromise.success(executionResponse);
        compensableSagaPromise.success(executionResponse);

        compensableSagaPromise.cancelDueToOtherFailure(new IllegalStateException());
        compensableSagaPromise.cancelDueToOtherFailure(new IllegalStateException());


        // then
        verify(executeOps, only().description("The request should be executed once."))
                .accept(anyInt());
        verify(promise, times(1).description("The success() method should be executed once."))
                .setSuccess(any(SagaMessage.class));
        verify(promise, never().description("The setFailure() method should never be executed."))
                .setFailure(any(Exception.class));
        verify(compensateOps, only().description("The compensation request should be executed once."))
                .accept(anyInt());
        assertFalse(compensableSagaPromise.isDone(), "The isDone() method should return false.");
    }

    @Test
    @DisplayName("CompensableSagaPromise - 실행 -> 응답 안받음 -> 다른 서비스 실패 -> 보상 실행 대기 - isDone() false 반환")
    void should_success_when_executionAndFailure() throws ExecutionException, InterruptedException {
        // given
        when(promise.isDone()).thenReturn(false);
        when(promise.isSuccess()).thenReturn(false);

        // when
        compensableSagaPromise.execute(10);
        compensableSagaPromise.execute(10);

        boolean isExecutionPending = compensableSagaPromise.isDone();

        compensableSagaPromise.cancelDueToOtherFailure(new IllegalStateException());
        compensableSagaPromise.cancelDueToOtherFailure(new IllegalStateException());

        // then
        verify(executeOps, only().description("The request should be executed once."))
                .accept(anyInt());
        verify(compensateOps, never().description("The compensation request should never be executed."))
                .accept(anyInt());
        verify(promise, never().description("The setSuccess() method should never be executed."))
                .setSuccess(any());
        verify(promise, never().description("The setFailure method should never be executed."))
                .setFailure(any());
        assertFalse(isExecutionPending, "The isDone() method should return false.");
    }

    @Test
    @DisplayName("CompensableSagaPromise - 실행 -> 응답 안받음 -> 다른 서비스 실패 -> 실행 응답 받음 -> 보상 실행 - isDone() false 반환")
    void should_compensateOnce_when_executionAndFailureAndDelayedResponse() {
        // given
        when(promise.isDone())
                .thenReturn(
                        false, false, false, false, true, true
                );
        when(promise.isSuccess()).thenReturn(false);

        SagaMessage<?, ?> executionResponse = mock(SagaMessage.class);
        when(executionResponse.stepType()).thenReturn(StepType.EXECUTE);

        // when
        compensableSagaPromise.execute(10);
        compensableSagaPromise.execute(10);

        boolean isExecutionPending = compensableSagaPromise.isDone();

        compensableSagaPromise.cancelDueToOtherFailure(new IllegalStateException());
        compensableSagaPromise.cancelDueToOtherFailure(new IllegalStateException());

        compensableSagaPromise.success(executionResponse);
        compensableSagaPromise.success(executionResponse);

        boolean isCompensationPending = compensableSagaPromise.isDone();

        // then
        verify(executeOps, only().description("The request should be executed once."))
                .accept(anyInt());
        verify(compensateOps, only().description("The compensation request should be executed once."))
                .accept(anyInt());
        verify(promise, times(1).description("The setSuccess() method should be executed once."))
                .setSuccess(any());
        verify(promise, never().description("The setFailure method should never be executed."))
                .setFailure(any());
        assertFalse(isExecutionPending, "The isDone() method should return false.");
        assertFalse(isCompensationPending, "The isDone() method should return false.");
    }

    @Test
    @DisplayName("CompensableSagaPromise - 실행 -> 응답 안받음 -> 다른 서비스 실패 -> 실행 응답 -> 보상 실행 -> 보상 응답 - isDone() false 반환")
    void should_expectedBehavior_when_stateUnderTest() {
        // given
        when(promise.isDone()).thenReturn(false, false, false, false, true);
        when(promise.isSuccess()).thenReturn(false);

        SagaMessage<?, ?> executionResponse = mock(SagaMessage.class);
        when(executionResponse.stepType()).thenReturn(StepType.EXECUTE);

        SagaMessage<?, ?> compensationResponse = mock(SagaMessage.class);
        when(compensationResponse.stepType()).thenReturn(StepType.COMPENSATE);

        // when
        compensableSagaPromise.execute(10);
        compensableSagaPromise.execute(10);

        boolean isExecutionPending = compensableSagaPromise.isDone();

        compensableSagaPromise.cancelDueToOtherFailure(new IllegalStateException());
        compensableSagaPromise.cancelDueToOtherFailure(new IllegalStateException());

        compensableSagaPromise.success(executionResponse);
        compensableSagaPromise.success(executionResponse);

        compensableSagaPromise.success(compensationResponse);
        compensableSagaPromise.success(compensationResponse);

        boolean isCompensationSuccess = compensableSagaPromise.isDone();

        // then
        verify(executeOps, only().description("The request should be executed once."))
                .accept(anyInt());
        verify(compensateOps, only().description("The compensation request should be executed once."))
                .accept(anyInt());
        verify(promise, times(1).description("The setSuccess() method should be executed once."))
                .setSuccess(any());
        verify(promise, never().description("The setFailure method should never be executed."))
                .setFailure(any());
        assertFalse(isExecutionPending, "The isDone() method should return false.");
        assertTrue(isCompensationSuccess, "The isDone() method should return false.");
    }

    @Test
    @DisplayName("CompensableSagaPromise - 요청 전 실패 - 보상 요청 없음, isDone() true 반환")
    void should_fail_when_failureBeforeExecution() {
        // given
        when(promise.isDone())
                .thenReturn(false, true);

        // when
        compensableSagaPromise.cancelDueToOtherFailure(new IllegalStateException());
        compensableSagaPromise.cancelDueToOtherFailure(new IllegalStateException());

        // then
        verify(executeOps, never().description("Requests should never be executed"))
                .accept(anyInt());
        verify(promise, never().description("The setSuccess() method should never be executed."))
                .setSuccess(any(SagaMessage.class));
        verify(promise, times(1).description("The setFailure() method should be executed once."))
                .setFailure(any());
        verify(compensateOps, never().description("The compensation request should never be executed."))
                .accept(anyInt());
        assertTrue(compensableSagaPromise.isDone(), "The isDone() method should return true.");
    }
}