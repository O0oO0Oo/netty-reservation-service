package org.server.rsaga.saga.promise.impl;

import com.google.common.base.Preconditions;
import io.netty.util.concurrent.Promise;
import org.server.rsaga.saga.promise.AbstractSagaPromise;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * <pre>
 * 기존 Promise 의 경우 setSuccess 를 할경우 fail 로 설정하지 못하며,
 * 그에 따른 listener 를 호출하지 못한다.
 * 따라서 상태 설정 이후에도 추가적인 동작을 할 수 있도록 구성하였다.
 * </pre>
 */
public final class CompensableSagaPromise<I, R> extends AbstractSagaPromise<I, R> {
    private final Consumer<I> compensate;
    private final AtomicBoolean isCompensationExecuted = new AtomicBoolean(false);
    private volatile boolean isCompensatedSuccessfully = false;

    public CompensableSagaPromise(Promise<R> executeNextPromise, Consumer<I> execute, Consumer<I> compensate) {
        super(executeNextPromise, execute);
        this.compensate = compensate;
    }

    @Override
    public void success(R result) {
        Preconditions.checkNotNull(result, "The result value should not be null.");
        if(!executePromise.isDone() && Objects.nonNull(input) && !isCompensationExecuted.get()) {
            executePromise.setSuccess(result);
        }

        if (isCompensationExecuted.get()) {
            isCompensatedSuccessfully = true;
        }
    }

    /**
     * 현재 Promise 에 등록된 동작이 완료/실행 중 일때는 이를 되돌리기 위한 Compensate 가 필요하다.
     * 두번 실행 방지.
     */
    @Override
    public void failure(Throwable cause) {
        if ((executePromise.isSuccess() || Objects.nonNull(input)) && isCompensationExecuted.compareAndSet(false,true)) {
            compensate.accept(input);
        }
        else {
            executePromise.setFailure(cause);
        }
    }

    @Override
    public boolean isDone() {
        return super.isDone() && hasCompensationSucceeded();
    }

    private boolean hasCompensationSucceeded() {
        if (isCompensationExecuted.get()) {
            return isCompensatedSuccessfully;
        }
        else{
            return true;
        }
    }
}