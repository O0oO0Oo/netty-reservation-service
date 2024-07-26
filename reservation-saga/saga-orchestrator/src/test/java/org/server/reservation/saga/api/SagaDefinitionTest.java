package org.server.reservation.saga.api;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import kotlin.jvm.functions.Function2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.server.reservation.saga.api.SagaDefinition;
import org.server.reservation.saga.api.factory.SagaDefinitionFactoryImpl;
import org.server.reservation.saga.message.MessageProducer;
import org.server.reservation.saga.message.SagaMessage;
import org.server.reservation.saga.promise.SagaPromise;
import org.server.reservation.saga.promise.factory.SagaPromiseFactory;
import org.server.reservation.saga.promise.factory.SagaPromiseFactoryImpl;
import org.server.reservation.saga.step.impl.StepType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * <pre>
 *
 * {@link org.server.reservation.saga.promise.SagaPromise}
 * {@link SagaPromiseFactory}
 * {@link org.server.reservation.saga.promise.strategy.SagaPromiseCreationStrategy}
 * {@link org.server.reservation.saga.step.SagaStep}
 * {@link SagaDefinition}
 *
 * 위 5가지 클래스들의 통합 테스트로 다음을 테스트 한다.
 * - 다른 서비스로의 요청시 요청과 같은 응답을 반환하는 Echo Server 처럼 동작한다고 가정
 * - 다른 서비스에서 이 Message 를 받고 이에 대한 응답을 기록한다.
 *
 * </pre>
 */
@ExtendWith(MockitoExtension.class)
class SagaDefinitionTest {

    @Mock
    MessageProducer<Integer> messageProducer;

    EventLoopGroup eventLoopGroup;
    SagaPromiseFactory sagaPromiseFactory;
    SagaDefinitionFactoryImpl<Integer> sagaDefinitionFactory;

    @BeforeEach
    void beforeEach() {
        eventLoopGroup = new NioEventLoopGroup();
        sagaPromiseFactory = new SagaPromiseFactoryImpl(eventLoopGroup);
        sagaDefinitionFactory = new SagaDefinitionFactoryImpl<>();
        sagaDefinitionFactory.setSagaPromiseFactory(sagaPromiseFactory);
    }

    /**
     *<pre>
     *<code>
     *
     * SagaPromises - SagaPromise[0, 1, 2, 3, 4, 5, 6, 7] 가 있다.
     * 일반 Promise 는 이전의 작업이 완료되면 요청을 보내지만,
     * 여러 결과를 받아 실행해야 하는 Aggregate Promise 는 이전 단계가 모두 완료되면,
     * 결과들을 받아 요청을 보낸다.
     *
     *          +-> [1] --+
     *          |         | ------------> [6] --------> [7]
     *          |    +----+                ^
     *          |    |                     |
     * [0] ---> +-> [2]                    |
     *          |    |                     |
     *          |    +----+                |
     *          |         | ---> [4] ---> [5 (Compensable)]
     *          +-> [3] --+
     *</code>
     *</pre>
     */
    @Test
    @DisplayName("복잡한 실행 체인 설정 - 실행 성공과 최종 값 검증 - 성공.")
    void should_allPromiseChainsSuccess_when_validResponseReceived() throws InterruptedException, ExecutionException {
        // given
        Map<Integer, SagaMessage<Integer>> results = new HashMap<>();
        Map<Integer, Boolean> isExecuted = new HashMap<>();
        Map<Integer, CountDownLatch> lock = new HashMap<>();

        for (int i = 1; i < 8; i++) {
            @SuppressWarnings("unchecked")
            SagaMessage<Integer> resultMock = Mockito.mock(SagaMessage.class);
            results.put(i, resultMock);
            isExecuted.put(i, false);
            lock.put(i, new CountDownLatch(1));
        }

        Function2<Integer, SagaMessage<Integer>, SagaMessage<Integer>> operation = (id, message) -> {
            isExecuted.put(id, true);
            lock.get(id).countDown();
            return message;
        };

        Function2<Integer, List<SagaMessage<Integer>>, SagaMessage<Integer>> aggregateOperation = (id, messages) -> {
            isExecuted.put(id, true);
            lock.get(id).countDown();
            return messages.get(0);
        };

        SagaDefinition<Integer> sagaDefinition = sagaDefinitionFactory
                .addStep("step1", "none", operation, messageProducer, StepType.EXECUTE)
                .addStep("step2", "none", operation, messageProducer, StepType.EXECUTE)
                .addStep("step3", "none", operation, messageProducer, StepType.EXECUTE)
                .addStep("step4", "none", aggregateOperation, messageProducer, StepType.EXECUTE, "step2", "step3")
                .addStep("step5", "none", operation, messageProducer, StepType.EXECUTE, "step4")
                .addStep("step5", "none", operation, messageProducer, StepType.COMPENSATE)
                .addStep("step6", "none", aggregateOperation, messageProducer, StepType.EXECUTE, "step1", "step2", "step5")
                .addStep("step7", "none", operation, messageProducer, StepType.EXECUTE, "step6")
                .getSagaDefinition();

        SagaPromise<?, SagaMessage<Integer>>[] sagaPromises = sagaDefinition.initializeSaga();

        @SuppressWarnings("unchecked")
        SagaMessage<Integer> responseMock = Mockito.mock(SagaMessage.class);

        int expectResult = 10;
        Mockito.when(results.get(7).getPayload()).thenReturn(expectResult);

        // when
        CompletableFuture<Void> cf0 = CompletableFuture
                .runAsync(() -> sagaPromises[0].success(responseMock));

        CompletableFuture<Void> cf1 = cf0.thenRunAsync(() -> {
            latchWait(lock.get(1));
            sagaPromises[1].success(results.get(1));
        });
        CompletableFuture<Void> cf2 = cf0.thenRun(() -> {
            latchWait(lock.get(2));
            sagaPromises[2].success(results.get(2));
        });
        CompletableFuture<Void> cf3 = cf0.thenRun(() -> {
            latchWait(lock.get(3));
            sagaPromises[3].success(results.get(3));
        });

        CompletableFuture<Void> cf4 = CompletableFuture.allOf(cf2, cf3).thenRun(() -> {
            latchWait(lock.get(4));
            sagaPromises[4].success(results.get(4));
        });

        CompletableFuture<Void> cf5 = cf4.thenRun(() -> {
            latchWait(lock.get(5));
            sagaPromises[5].success(results.get(5));
        });

        CompletableFuture<Void> cf6 = CompletableFuture.allOf(cf1, cf2, cf5).thenRun(() -> {
            latchWait(lock.get(6));
            sagaPromises[6].success(results.get(6));
        });

        CompletableFuture<Void> cf7 = cf6.thenRun(() -> {
            latchWait(lock.get(7));
            sagaPromises[7].success(results.get(7));
        });

        //wait
        cf7.join();
        eventLoopGroup.shutdownGracefully();
        eventLoopGroup.terminationFuture().sync();

        // then
        SagaMessage<Integer> actualResult = sagaPromises[7].get();
        Mockito.verify(messageProducer, Mockito.times(7)).produce(Mockito.notNull(), Mockito.notNull());
        assertEquals(expectResult, actualResult.getPayload(), "The result must be 10.");
        assertTrue(isExecuted.values().stream().allMatch(v -> v), "All steps must be executed.");
    }

    /**
     * <pre>
     * <code>
     *
     *                                    3. An error occurred after execution
     *          +-> [1] -+                2. execute step4
     *          |        |                            |
     * [0] ---> |        + --> [3 Compensable] --> [4 (x)] --> [5]
     *          |        |            |
     *          +-> [2] -+       1. execute step3, success
     *                           4. Compensating operation
     *</code>
     *</pre>
     */
    @Test
    @DisplayName("CompensableSagaPromise 가 있는 실행 체인 - 중간 단계 실패 - 보상 요청 성공")
    void should_failAndCompensating_when_failureResponseReceived() throws InterruptedException {
        // given
        Map<Integer, CountDownLatch> lock = new HashMap<>();
        for (int i = 1; i < 5; i++) {
            lock.put(i, new CountDownLatch(1));
        }

        String exceptionMessage = "An error occurred.";
        IllegalArgumentException exception = new IllegalArgumentException(exceptionMessage);
        AtomicInteger executedCount = new AtomicInteger();
        AtomicInteger isCompensated = new AtomicInteger(0);

        Function2<Integer, SagaMessage<Integer>, SagaMessage<Integer>> operation = (id, message) -> {
            executedCount.incrementAndGet();
            lock.get(id).countDown();
            return message;
        };

        Function2<Integer, List<SagaMessage<Integer>>, SagaMessage<Integer>> step3ExecuteOperation = (id, messages) -> {
            isCompensated.incrementAndGet();
            executedCount.incrementAndGet();
            lock.get(id).countDown();
            return messages.get(0);
        };

        Function2<Integer, List<SagaMessage<Integer>>, SagaMessage<Integer>> step3CompensateOperation = (id, messages) -> {
            isCompensated.decrementAndGet();
            executedCount.incrementAndGet();
            return messages.get(0);
        };

        SagaDefinition<Integer> sagaDefinition = sagaDefinitionFactory
                .addStep("step1", "none", operation, messageProducer, StepType.EXECUTE)
                .addStep("step2", "none", operation, messageProducer, StepType.EXECUTE)
                .addStep("step3", "none", step3ExecuteOperation, messageProducer, StepType.EXECUTE, "step1", "step2")
                .addStep("step3", "none", step3CompensateOperation, messageProducer, StepType.COMPENSATE)
                .addStep("step4", "none", operation, messageProducer, StepType.EXECUTE, "step3")
                .addStep("step5", "none", operation, messageProducer, StepType.EXECUTE, "step4")
                .getSagaDefinition();

        SagaPromise<?, SagaMessage<Integer>>[] sagaPromises = sagaDefinition.initializeSaga();

        @SuppressWarnings("unchecked")
        SagaMessage<Integer> responseMock = Mockito.mock(SagaMessage.class);

        // when
        CompletableFuture<Void> cf0 = CompletableFuture
                .runAsync(() -> sagaPromises[0].success(responseMock));

        CompletableFuture<Void> cf1 = cf0.thenRun(() -> {
            latchWait(lock.get(1));
            sagaPromises[1].success(responseMock);
        });
        CompletableFuture<Void> cf2 = cf0.thenRun(() -> {
            latchWait(lock.get(2));
            sagaPromises[2].success(responseMock);
        });

        CompletableFuture<Void> cf3 = CompletableFuture.allOf(cf1, cf2).thenRun(() -> {
            latchWait(lock.get(3));
            sagaPromises[3].success(responseMock);
        });

        CompletableFuture<Void> cf4 = cf3.thenRun(() -> {
            latchWait(lock.get(4));
            // 요청을 보내고 나서 응답으로 에러를 받음. 모두 실패
            for (SagaPromise<?, SagaMessage<Integer>> sagaPromise : sagaPromises) {
                sagaPromise.failure(exception);
            }
        });

        // wait
        cf4.join();
        eventLoopGroup.shutdownGracefully();
        eventLoopGroup.terminationFuture().sync();

        // then
        IllegalArgumentException assertThrows = assertThrows(IllegalArgumentException.class, sagaPromises[4]::get);
        Mockito.verify(messageProducer, Mockito.times(5)).produce(Mockito.notNull(), Mockito.notNull());
        assertEquals(exceptionMessage, assertThrows.getMessage());
        assertEquals(0, isCompensated.get(), "Since the Compensate Operation was executed, it should be 0.");
        assertEquals(5, executedCount.get(), "The operation must be executed four times.");
    }

    /**
     * <pre>
     * <code>
     *
     *              +-> [1 (o)] -+        Response Received
     *              |            |              |
     * [0 (o)] ---> |            + --> [3] --> [4] --> [5]
     *              |            |      |
     *              +-> [2 (o)] -+    Executed but
     *                                response has not been
     *                                received yet.
     *</code>
     *</pre>
     */
    @Test
    @DisplayName("실행 체인 - 이전 단계가 완료되지 않았는데, 응답을 받았을 때 - 실패")
    void should_fail_when_receiveResponseWhenDependentsNotCompleted() throws InterruptedException {
        // given
        AtomicInteger executeCount = new AtomicInteger();
        Map<Integer, CountDownLatch> lock = new HashMap<>();
        for (int i = 1; i < 4; i++) {
            lock.put(i, new CountDownLatch(1));
        }

        Function2<Integer, SagaMessage<Integer>, SagaMessage<Integer>> operation = (id, message) -> {
            executeCount.incrementAndGet();
            lock.get(id).countDown();
            return message;
        };

        Function2<Integer, List<SagaMessage<Integer>>, SagaMessage<Integer>> aggregateOperation = (id, messages) -> {
            executeCount.incrementAndGet();
            lock.get(id).countDown();
            return messages.get(0);
        };

        SagaDefinition<Integer> sagaDefinition = sagaDefinitionFactory
                .addStep("step1", "none", operation, messageProducer, StepType.EXECUTE)
                .addStep("step2", "none", operation, messageProducer, StepType.EXECUTE)
                .addStep("step3", "none", aggregateOperation, messageProducer, StepType.EXECUTE, "step1" ,"step2")
                .addStep("step4", "none", operation, messageProducer, StepType.EXECUTE, "step3")
                .addStep("step5", "none", operation, messageProducer, StepType.EXECUTE, "step4")
                .getSagaDefinition();

        SagaPromise<?, SagaMessage<Integer>>[] sagaPromises = sagaDefinition.initializeSaga();

        @SuppressWarnings("unchecked")
        SagaMessage<Integer> responseMock = Mockito.mock(SagaMessage.class);

        // when
        CompletableFuture<Void> cf0 = CompletableFuture.runAsync(
                () -> sagaPromises[0].success(responseMock)
        );

        CompletableFuture<Void> cf1 = cf0.thenRun(() -> {
            latchWait(lock.get(1));
            sagaPromises[1].success(responseMock);
        });
        CompletableFuture<Void> cf2 = cf0.thenRun(() -> {
            latchWait(lock.get(2));
            sagaPromises[2].success(responseMock);
        });

        latchWait(lock.get(3));
        CompletableFuture<Void> cf4 = CompletableFuture.allOf(cf1, cf2).thenRun(() -> sagaPromises[4].success(responseMock));

        // wait
        cf4.join();
        eventLoopGroup.shutdownGracefully();
        eventLoopGroup.terminationFuture().sync();

        // then
        assertEquals(3, executeCount.get(), "The operation must be executed 3 times [1, 2, 3].");
    }

    /**
     * <pre>
     *
     * 원래의 시스템이라면 다음과 같이 요청을 보내고나서 응답을 받는다.
     * [4] --> request -->  +---------+
     *                      |Service A| processing
     * [4] <-- response <-- +---------+
     *
     * 하지만 현재 테스트에서는 요청을 보내는 객체는 Mock 으로 되어있고,
     * 비동기로 진행되기 때문에, 요청과 응답 사이의 실행순서 조정이 필요하다.
     * 따라서 요청 후 latch 의 완료에 따라 응답이 진행되게 하는
     * 실행 순서 로직이 필요하다.
     *
     * </pre>
     * @param latch
     */
    private void latchWait(CountDownLatch latch) {
        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}