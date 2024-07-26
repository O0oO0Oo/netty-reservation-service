package org.server.reservation.saga.promise.impl;

import io.netty.util.concurrent.Promise;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.server.reservation.saga.promise.SagaPromise;

import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

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
@ExtendWith(MockitoExtension.class)
class CompensableSagaPromiseTest {

    @Mock
    Promise<Integer> promise;
    @Mock
    Consumer<Integer> executeOps;
    @Mock
    Consumer<Integer> compensateOps;

    SagaPromise<Integer, Integer> compensableSagaPromise;

    @BeforeEach
    void beforeEach() {
        compensableSagaPromise = new CompensableSagaPromise<>(promise, executeOps, compensateOps);
    }

    @Test
    @DisplayName("CompensableSagaPromise - 요청 후 두번 Compensate 요청 - 한번의 compensate 요청 성공")
    void should_success_when_executeRequestThenCompensateRequest() {
        // given
        Mockito.when(promise.isSuccess()) // compensableSagaPromise.failure()
                .thenReturn(false, false);

        // when
        compensableSagaPromise.execute(10);
        compensableSagaPromise.execute(10);

        compensableSagaPromise.failure(new IllegalStateException());
        compensableSagaPromise.failure(new IllegalStateException());

        // then
        Mockito.verify(executeOps, Mockito.only().description("The request should be made once."))
                .accept(Mockito.anyInt());
        Mockito.verify(compensateOps, Mockito.only().description("The compensation request should be made once."))
                .accept(Mockito.anyInt());
    }

    @Test
    @DisplayName("CompensableSagaPromise - 응답 완료 Compensate 요청 - 성공")
    void should_success_when_executeRequestAndReceiveResponseThenCompensateRequest() throws ExecutionException, InterruptedException {
        // given
        Mockito.when(promise.isDone()) // compensableSagaPromise.success()
                .thenReturn(false)
                .thenReturn(true, true, true);

        Mockito.when(promise.isSuccess()) // compensableSagaPromise.failure()
                .thenReturn(true, true);

        Mockito.when(promise.get()).thenReturn(10);

        // when
        compensableSagaPromise.execute(10);
        compensableSagaPromise.execute(10);

        compensableSagaPromise.success(10);
        compensableSagaPromise.success(10);
        compensableSagaPromise.success(10);

        compensableSagaPromise.failure(new IllegalStateException());
        compensableSagaPromise.failure(new IllegalStateException());

        compensableSagaPromise.success(10);

        // then
        Integer result = compensableSagaPromise.get();
        assertEquals(10, result);
        Mockito.verify(executeOps, Mockito.only().description("The request should be made once."))
                .accept(Mockito.anyInt());
        Mockito.verify(promise, Mockito.times(1).description("The setSuccess() method should be executed once."))
                .setSuccess(Mockito.anyInt());
        Mockito.verify(compensateOps, Mockito.only().description("The compensation request should be made once."))
                .accept(Mockito.anyInt());
    }

    @Test
    @DisplayName("CompensableSagaPromise - 요청 전 Compensate 요청 - 실패")
    void should_fail_when_compensateRequestBeforeExecuteRequest() {
        // given
        Mockito.when(promise.isDone()) // compensableSagaPromise.success()
                .thenReturn(false);

        Mockito.when(promise.isSuccess()) // compensableSagaPromise.failure()
                .thenReturn(false, false);

        // when
        compensableSagaPromise.success(10);

        compensableSagaPromise.failure(new IllegalStateException());
        compensableSagaPromise.failure(new IllegalStateException());

        // then
        Mockito.verify(executeOps,Mockito.never().description("Requests should never be made"))
                .accept(Mockito.anyInt());
        Mockito.verify(promise, Mockito.never().description("The setSuccess() method should never be executed."))
                .setSuccess(Mockito.anyInt());
        Mockito.verify(compensateOps,Mockito.never().description("The compensation request should never be made."))
                .accept(Mockito.anyInt());
    }

    @Test
    @DisplayName("CompensableSagaPromise - Compensate 요청 후, 응답이 오지 않음 - isDone() return false")
    void should_returnFalse_when_compensateRequestButNotReceiveResponse() throws ExecutionException, InterruptedException {
        // given
        Mockito.when(promise.isDone()) // compensableSagaPromise.success()
                .thenReturn(false)
                .thenReturn(true);

        Mockito.when(promise.isSuccess()) // compensableSagaPromise.failure()
                .thenReturn(true, true);

        Mockito.when(promise.get()).thenReturn(10);

        // when
        compensableSagaPromise.execute(10);
        compensableSagaPromise.execute(10);

        compensableSagaPromise.success(10);
        compensableSagaPromise.success(10);

        compensableSagaPromise.failure(new IllegalStateException());
        compensableSagaPromise.failure(new IllegalStateException());

        // then
        Integer result = compensableSagaPromise.get();
        assertEquals(10, result);
        Mockito.verify(executeOps, Mockito.only().description("The request should be made once."))
                .accept(Mockito.anyInt());
        Mockito.verify(promise, Mockito.times(1).description("The setSuccess() method should be executed once."))
                .setSuccess(Mockito.anyInt());
        Mockito.verify(compensateOps, Mockito.only().description("The compensation request should be made once."))
                .accept(Mockito.anyInt());
        assertFalse(compensableSagaPromise.isDone(), "Since a compensating response has not yet been received, isDone() should return false");
    }

    @Test
    @DisplayName("CompensableSagaPromise - Compensate 요청 후, 응답 확인 - isDone() return true")
    void should_returnTrue_when_compensateRequestThenReceiveResponse() throws ExecutionException, InterruptedException {
        // given
        Mockito.when(promise.isDone()) // compensableSagaPromise.success()
                .thenReturn(false)
                .thenReturn(true, true, true, true);

        Mockito.when(promise.isSuccess()) // compensableSagaPromise.failure()
                .thenReturn(true, true);

        Mockito.when(promise.get()).thenReturn(10);

        // when
        compensableSagaPromise.execute(10);
        compensableSagaPromise.execute(10);

        compensableSagaPromise.success(10);
        compensableSagaPromise.success(10);
        compensableSagaPromise.success(10);

        compensableSagaPromise.failure(new IllegalStateException());
        compensableSagaPromise.failure(new IllegalStateException());

        compensableSagaPromise.success(10);
        compensableSagaPromise.success(10);

        // then
        Integer result = compensableSagaPromise.get();
        assertEquals(10, result);
        Mockito.verify(executeOps, Mockito.only().description("The request should be made once."))
                .accept(Mockito.any(Integer.class));
        Mockito.verify(promise, Mockito.times(1).description("The setSuccess() method should be executed once."))
                .setSuccess(Mockito.any(Integer.class));
        Mockito.verify(compensateOps, Mockito.only().description("The compensation request should be made once."))
                .accept(Mockito.any(Integer.class));
        assertTrue(compensableSagaPromise.isDone(), "A compensating response has been received, isDone() should return true");
    }
}