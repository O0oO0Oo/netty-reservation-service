package org.server.rsaga.saga.promise.impl;

import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.internal.ObjectUtil;
import org.server.rsaga.saga.api.SagaMessage;
import org.server.rsaga.saga.promise.SagaPromise;

import java.util.ArrayList;
import java.util.List;

/**
 * <pre>
 * io.netty-common 4.1.1 의 PromiseCombiner,
 * {@link io.netty.util.concurrent.PromiseAggregator} 를 참고하여 작성함.
 *
 * 이전 단계의 SagaPromise 의 결과들을 모으고, 모든 SagaPromise 가 완료되면
 * 이 결과물을 aggregatePromise 의 실행 입력값으로 설정하여 실행한다.
 * </pre>
 */
public final class SagaPromiseAggregator<I extends SagaMessage<?, ?>, R extends SagaMessage<?, ?>> {
    private int expectedCount;
    private SagaPromise<List<I>, R> aggregatePromise;
    private final List<I> results = new ArrayList<>();
    private Throwable cause;

    private final GenericFutureListener<Future<I>> listener = future ->  {
        if (future.isSuccess()) {
            results.add(future.get());
        } else if (cause == null) {
            cause = future.cause();
        }

        if (results.size() == expectedCount && aggregatePromise != null) {
            tryPromise();
        }
    };

    private void tryPromise() {
        if(cause == null){
            aggregatePromise.execute(results);
        }
        else {
            aggregatePromise.cancelDueToOtherFailure(cause);
        }
    }

    public void finish(SagaPromise<List<I>, R> aggregatePromise) {
        ObjectUtil.checkNotNull(aggregatePromise, "aggregatePromise is null.");
        this.aggregatePromise = aggregatePromise;
        if (expectedCount == 0) {
            tryPromise();
        }
    }

    @SafeVarargs // 가변인자 타입 안정성 보장
    public final SagaPromiseAggregator<I, R> add(SagaPromise<?, I>... sagaPromises) {
        checkAddAllowed();
        for (SagaPromise<?, I> promise : sagaPromises) {
            ++expectedCount;
            promise.addListener(this.listener);
        }
        return this;
    }

    private void checkAddAllowed() {
        if (aggregatePromise != null) {
            throw new IllegalStateException("Adding promises is not allowed after finished adding");
        }
    }
}