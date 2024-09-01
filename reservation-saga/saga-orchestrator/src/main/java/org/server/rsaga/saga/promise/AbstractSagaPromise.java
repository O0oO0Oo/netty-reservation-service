package org.server.rsaga.saga.promise;

import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.concurrent.Promise;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.server.rsaga.saga.api.SagaMessage;

import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public abstract class AbstractSagaPromise<I, R extends SagaMessage<?, ?>> implements SagaPromise<I, R>{
    protected Promise<R> executePromise;
    private Consumer<I> execute;
    private boolean isExecutionPerformed = false;


    protected AbstractSagaPromise(Promise<R> executePromise, Consumer<I> execute) {
        this.executePromise = executePromise;
        this.execute = execute;
    }

    @Override
    public R get() throws ExecutionException, InterruptedException {
        return executePromise.await().get();
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
        if (!isExecutionPerformed) {
            isExecutionPerformed = true;
            execute.accept(input);
        }
    }

    @Override
    public void addListener(GenericFutureListener<? extends Future<R>> listener) {
        executePromise.addListener(listener);
    }

    @Override
    public Promise<R> getPromise() {
        return this.executePromise;
    }

    @Override
    public Throwable cause() {
        return executePromise.cause();
    }

    protected boolean isExecutionPerformed() {
        return isExecutionPerformed;
    }
}