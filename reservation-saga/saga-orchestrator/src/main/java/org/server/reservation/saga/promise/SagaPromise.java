package org.server.reservation.saga.promise;

import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import org.server.reservation.saga.promise.factory.SagaPromiseFactoryImpl;
import org.server.reservation.saga.promise.strategy.SagaPromiseCreationStrategy;
import org.server.reservation.saga.step.impl.StepType;

import java.util.concurrent.ExecutionException;

/**
 * {@link io.netty.util.concurrent.Promise} 의 기능 확장을 위한 Wrapper 클래스.<br>
 * Orchestration Saga 패턴을 위해 다음 예시와 같은 실행 체인을 형성한다.
 * <br>
 * <pre>
 * <code>
 *
 * 1, 2 번 SagaPromise 가 완료되면 4번이 execute 된다.
 * 2, 3 번 SagaPromise 가 완료되면 5, 6번이 execute 된다.
 *
 *                     +---> [1] ---+
 *                     |            |---> [4 (depends on 1,2)] ---+
 * [init promise] ---> +---> [2] ---+                             |---+
 *                     |            |---> [5 (depends on 2,3)] ---+   |
 *                     +---> [3] ---+                                 +---> [7]
 *                                  |                                 |
 *                                  +---> [6 (depends on 3)] ---------+
 *
 * 각 SagaPromise 는 execute 를 통해 각 단계에서 이전 단계의 결과를 바탕으로 다른 서비스로 요청을 보낸다.
 *
 * [1,2] done
 *  |
 *  v
 * [4] --------->+ request -------+
 *               |    Service A   |
 * [4] <---------+ response ------+
 *  |
 *  | wait until 5, 6 succeed
 *  v
 * [7] ----------> request Service B...
 *
 * </code>
 * </pre>
 *
 * <pre>
 * {@link io.netty.util.concurrent.Promise} 가 setSuccess 하면 등록된 listener 를 통해 다음 동작을 하도록 고안했었다.
 * 하지만 Promise 는 상태가 설정되고 나면 listener 를 호출할 수 없다.
 * 사가 패턴의 compensating transaction 은 성공 이후에도 다른 동작을 해야 한다.
 * 따라서 기존 Promise 의 기능을 활용하고 추가적인 동작을 위해 설계하였다.
 * </pre>
 *
 * <pre>
 * SagaPromise 동작을 추가하려면 다음 네가지를 추가해야 한다.
 * 1. {@link SagaPromise} 구현
 * 2. {@link SagaPromiseCreationStrategy}
 *    SagaPromise 생성 전략 패턴
 * 3. {@link StepType}
 *    StepType 에 맞는 SagaPromise 가 생성된다.
 * 4. {@link SagaPromiseFactoryImpl}
 *    의 ServiceLoader 에 등록하기 위해 core 모듈의 resources/META-INF/services 에 클래스를 등록해야 한다.
 *
 * </pre>
 *
 * todo : 동시성 문제 success, failure 의 순서를 지킬수 있게 해야함, success 와 failure 가 동시에 실행되지 않게 객체 수준의 동시성 해결해야함
 * @param <I> 실행 input 값
 * @param <R> 결과 값
 */
public interface SagaPromise<I, R> extends GenericFutureListener<Future<I>> {
    /**
     * SagaPromise 의 값을 획득
     *
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     */
    R get() throws ExecutionException, InterruptedException;

    /**
     * 현재 SagaPromise 를 완료 상태로 지정
     */
    void success(R result);

    boolean isDone();

    void execute(I response);
    /**
     * 현재 SagaPromise 를 실패 상태로 지정
     */
    void failure(Throwable cause);

    /**
     * 현재 SagaPromise 가 완료, 실패하면 실행할 listener 추가
     * @param listener {@link io.netty.util.concurrent.Promise} 의 listener
     */
    void addListener(GenericFutureListener<? extends Future<R>> listener);

    Throwable cause();
}