package org.server.rsaga.saga.promise;

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
import org.server.rsaga.messaging.message.Message;
import org.server.rsaga.saga.api.SagaMessage;
import org.server.rsaga.saga.promise.impl.DefaultSagaPromise;
import org.server.rsaga.saga.promise.impl.SagaPromiseAggregator;

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
@DisplayName("SagaPromiseAggregator Implementation Tests")
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
    void should_aggregatePromiseSuccess_when_dependencySagaIsSuccess() {
        // given
        CountDownLatch latch = new CountDownLatch(1);

        Consumer<SagaMessage<Integer, Integer>> executeOperation = Mockito.mock(Consumer.class);
        Promise<SagaMessage<Integer, Integer>> promise1 = eventLoopGroup.next().newPromise();
        Promise<SagaMessage<Integer, Integer>> promise2 = eventLoopGroup.next().newPromise();
        SagaPromise<SagaMessage<Integer, Integer>, SagaMessage<Integer, Integer>> sagaPromise1 = new DefaultSagaPromise<>(promise1, executeOperation);
        SagaPromise<SagaMessage<Integer, Integer>, SagaMessage<Integer, Integer>> sagaPromise2 = new DefaultSagaPromise<>(promise2, executeOperation);

        AtomicInteger result = new AtomicInteger();
        Consumer<List<SagaMessage<Integer, Integer>>> finishExecuteOperation = (messages) -> {
            result.set(messages.stream().mapToInt(Message::payload).sum());
            latch.countDown();
        };
        Promise<SagaMessage<Integer, Integer>> finishPromise = eventLoopGroup.next().newPromise();
        SagaPromise<List<SagaMessage<Integer, Integer>>, SagaMessage<Integer, Integer>> finishSagaPromise = new DefaultSagaPromise<>(finishPromise, finishExecuteOperation);

        SagaPromiseAggregator<SagaMessage<Integer, Integer>, SagaMessage<Integer, Integer>> sagaPromiseAggregator = new SagaPromiseAggregator<>();
        sagaPromiseAggregator.add(sagaPromise1);
        sagaPromiseAggregator.add(sagaPromise2);
        sagaPromiseAggregator.finish(finishSagaPromise);

        // when
        SagaMessage<Integer, Integer> response1 = SagaMessage.of(10, 10, Message.Status.RESPONSE_SUCCESS);

        CompletableFuture<Void> cf =
                CompletableFuture.runAsync(
                                () -> {
                                    sagaPromise1.execute(response1);
                                    sagaPromise1.success(response1);
                                }
                        )
                        .thenRunAsync(
                                () -> {
                                    sagaPromise2.execute(response1);
                                    sagaPromise2.success(response1);
                                }
                        );

        SagaMessage<Integer, Integer> response2 = SagaMessage.of(10, 20, Message.Status.RESPONSE_SUCCESS);
        CompletableFuture<Void> finishCf =
                cf.thenRun(() ->
                        {
                            try {
                                // finishExecuteOperation 과 실행순서 조정을 위해
                                latch.await();
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                            finishSagaPromise.success(response2);
                        }
                );

        // wait
        finishCf.join();

        // then
        assertTrue(promise1.isSuccess(), "promise1 must succeed.");
        assertTrue(promise2.isSuccess(), "promise2 must succeed.");
        assertTrue(finishPromise.isSuccess(), "finishPromise must succeed");
        assertEquals(20, result.get(), "The result should be 20.");
    }
}