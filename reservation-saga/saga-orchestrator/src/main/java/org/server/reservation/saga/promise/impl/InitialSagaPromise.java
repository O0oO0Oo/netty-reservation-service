package org.server.reservation.saga.promise.impl;

import com.google.common.base.Preconditions;
import io.netty.util.concurrent.Promise;
import org.server.reservation.saga.promise.AbstractSagaPromise;

import java.util.Objects;
import java.util.function.Consumer;

public final class InitialSagaPromise<I, R> extends AbstractSagaPromise<I, R> {

    public InitialSagaPromise(Promise<R> executePromise) {
        this(executePromise, null);
    }

    private InitialSagaPromise(Promise<R> executePromise, Consumer<I> execute) {
        super(executePromise, execute);
    }

    @Override
    public void success(R result) {
        Preconditions.checkNotNull(result, "The result value should not be null.");
        if(!executePromise.isDone()) {
            executePromise.setSuccess(result);
        }
    }

    @Override
    public void failure(Throwable cause) {
        if (!executePromise.isSuccess() && Objects.nonNull(input)) {
            executePromise.setFailure(cause);
        }
    }
}