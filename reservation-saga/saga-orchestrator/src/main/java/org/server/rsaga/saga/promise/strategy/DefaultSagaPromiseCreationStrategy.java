package org.server.rsaga.saga.promise.strategy;

import io.netty.channel.EventLoop;
import io.netty.util.concurrent.Promise;
import org.server.rsaga.saga.promise.SagaPromise;
import org.server.rsaga.saga.promise.impl.DefaultSagaPromise;
import org.server.rsaga.saga.step.impl.StepType;

import java.util.Map;
import java.util.function.Consumer;

public class DefaultSagaPromiseCreationStrategy implements SagaPromiseCreationStrategy {
    @Override
    public <I, R> SagaPromise<I, R> createSagaPromise(Map<StepType, Consumer<I>> sagaSteps, EventLoop eventLoop) {
        Promise<R> executePromise = eventLoop.newPromise();
        return new DefaultSagaPromise<>(
                executePromise,
                sagaSteps.get(StepType.EXECUTE));
    }

    @Override
    public StepType getStepType() {
        return StepType.EXECUTE;
    }
}