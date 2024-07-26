package org.server.reservation.saga.promise.impl;

import com.google.common.base.Preconditions;
import io.netty.util.concurrent.Promise;
import org.server.reservation.saga.promise.AbstractSagaPromise;

import java.util.Objects;
import java.util.function.Consumer;

public final class DefaultSagaPromise<I, R> extends AbstractSagaPromise<I, R> {

    public DefaultSagaPromise(Promise<R> executeNextPromise, Consumer<I> execute) {
        super(executeNextPromise, execute);
    }

    @Override
    public void success(R result) {
        Preconditions.checkNotNull(result, "The result value should not be null.");
        if(!executePromise.isDone() && Objects.nonNull(input)) {
            executePromise.setSuccess(result);
        }
    }

    @Override
    public void failure(Throwable cause) {
        if (!executePromise.isDone() && Objects.nonNull(input)) {
            executePromise.setFailure(cause);
        }
    }
}