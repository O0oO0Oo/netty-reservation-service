package org.server.reservation.saga.promise;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.Promise;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.server.reservation.saga.promise.impl.DefaultSagaPromise;
import org.server.reservation.saga.promise.impl.SagaPromiseAggregator;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 이전의 수행되어야 할 작업들이 여러개인 Promise 를 설정하는 {@link SagaPromiseAggregator} 의 테스트
 */
@ExtendWith(MockitoExtension.class)
class SagaPromiseAggregatorTest {

    EventLoopGroup eventLoopGroup;
    @BeforeEach
    void beforeEach() {
        eventLoopGroup = new NioEventLoopGroup();
    }

    @AfterEach
    void afterEach() {
        eventLoopGroup.shutdownGracefully();
    }

    @Test
    @DisplayName("SagaPromises - 의존하는 Promise 모두 성공 - AggregatePromise 동작 실행")
    void should_aggregatePromiseSuccess_when_dependencySagaIsSuccess() throws InterruptedException {
        // given
        CountDownLatch latch = new CountDownLatch(1);

        Consumer<Integer> executeOperation = Mockito.mock(Consumer.class);
        Promise<Integer> promise1 = eventLoopGroup.next().newPromise();
        Promise<Integer> promise2 = eventLoopGroup.next().newPromise();
        SagaPromise<Integer, Integer> sagaPromise1 = new DefaultSagaPromise<>(promise1, executeOperation);
        SagaPromise<Integer, Integer> sagaPromise2 = new DefaultSagaPromise<>(promise2, executeOperation);

        AtomicInteger result = new AtomicInteger();
        Consumer<List<Integer>> finishExecuteOperation = (messages) -> {
            result.set(messages.stream().mapToInt(Integer::intValue).sum());
            latch.countDown();
        };
        Promise<Integer> finishPromise = eventLoopGroup.next().newPromise();
        SagaPromise<List<Integer>, Integer> finishSagaPromise = new DefaultSagaPromise<>(finishPromise, finishExecuteOperation);

        SagaPromiseAggregator<Integer, Integer> sagaPromiseAggregator = new SagaPromiseAggregator<>();
        sagaPromiseAggregator.add(sagaPromise1);
        sagaPromiseAggregator.add(sagaPromise2);
        sagaPromiseAggregator.finish(finishSagaPromise);

        // when
        CompletableFuture<Void> cf =
                CompletableFuture.runAsync(
                                () -> {
                                    sagaPromise1.execute(10);
                                    sagaPromise1.success(10);
                                }
                        )
                        .thenRunAsync(
                                () -> {
                                    sagaPromise2.execute(10);
                                    sagaPromise2.success(10);
                                }
                        );
        CompletableFuture<Void> finishCf =
                cf.thenRun(() ->
                        {
                            try {
                                // finishExecuteOperation 과 실행순서 조정을 위해
                                latch.await();
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                            finishSagaPromise.success(20);
                        }
                );

        // wait
        finishCf.join();
        eventLoopGroup.shutdownGracefully();
        eventLoopGroup.terminationFuture().sync();

        // then
        assertTrue(promise1.isSuccess(), "promise1 must succeed.");
        assertTrue(promise2.isSuccess(), "promise2 must succeed.");
        assertTrue(finishPromise.isSuccess(), "finishPromise must succeed");
        assertEquals(20, result.get(), "The result should be 20.");
    }
}