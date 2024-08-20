package org.server.rsaga.saga.promise.impl;

import com.google.common.base.Preconditions;
import io.netty.util.concurrent.Promise;
import org.server.rsaga.saga.api.SagaMessage;
import org.server.rsaga.saga.promise.AbstractSagaPromise;

import java.util.function.Consumer;

public final class DefaultSagaPromise<I, R extends SagaMessage<?, ?>> extends AbstractSagaPromise<I, R> {

    public DefaultSagaPromise(Promise<R> executeNextPromise, Consumer<I> execute) {
        super(executeNextPromise, execute);
    }

    @Override
    public void success(R result) {
        Preconditions.checkNotNull(result, "The result value should not be null.");
        if(!executePromise.isDone() && isExecutionPerformed()) {
            executePromise.setSuccess(result);
        }
    }

    @Override
    public void cancelDueToOtherFailure(Throwable cause) {
        if (!executePromise.isDone()) {
            executePromise.setFailure(cause);
        }
    }

    @Override
    public void failure(R result, Throwable cause) {
        if (!executePromise.isDone()) {
            executePromise.setFailure(cause);
        }
    }
}