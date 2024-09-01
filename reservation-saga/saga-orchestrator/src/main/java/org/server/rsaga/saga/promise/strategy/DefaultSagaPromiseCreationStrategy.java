package org.server.rsaga.saga.promise.strategy;

import io.netty.util.concurrent.EventExecutorGroup;
import io.netty.util.concurrent.Promise;
import org.server.rsaga.saga.api.SagaMessage;
import org.server.rsaga.saga.promise.SagaPromise;
import org.server.rsaga.saga.promise.impl.DefaultSagaPromise;
import org.server.rsaga.saga.step.impl.StepType;

import java.util.Map;
import java.util.function.Consumer;

public class DefaultSagaPromiseCreationStrategy implements SagaPromiseCreationStrategy {
    @Override
    public <I, R extends SagaMessage<?, ?>> SagaPromise<I, R> createSagaPromise(Map<StepType, Consumer<I>> sagaSteps, EventExecutorGroup eventExecutorGroup) {
        Promise<R> executePromise = eventExecutorGroup.next().newPromise();
        return new DefaultSagaPromise<>(
                executePromise,
                sagaSteps.get(StepType.EXECUTE));
    }

    @Override
    public StepType getStepType() {
        return StepType.EXECUTE;
    }
}