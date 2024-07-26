package org.server.reservation.saga.promise;

import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.concurrent.Promise;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public abstract class AbstractSagaPromise<I, R> implements SagaPromise<I, R>{
    protected Promise<R> executePromise;
    private Consumer<I> execute;
    protected I input;

    protected AbstractSagaPromise(Promise<R> executePromise, Consumer<I> execute) {
        this.executePromise = executePromise;
        this.execute = execute;
    }

    @Override
    public R get() throws ExecutionException, InterruptedException {
        executePromise.sync();
        return executePromise.get();
    }

    @Override
    public void operationComplete(Future<I> future) throws Exception {
        if(future.isSuccess()) {
            execute(future.get());
        }
        else {
            executePromise.setFailure(future.cause());
        }
    }

    @Override
    public boolean isDone() {
        return executePromise.isDone();
    }

    @Override
    public void execute(I input) {
        if (this.input == null) {
            this.input = input;
            execute.accept(input);
        }
    }

    @Override
    public void addListener(GenericFutureListener<? extends Future<R>> listener) {
        executePromise.addListener(listener);
    }

    @Override
    public Throwable cause() {
        return executePromise.cause();
    }
}