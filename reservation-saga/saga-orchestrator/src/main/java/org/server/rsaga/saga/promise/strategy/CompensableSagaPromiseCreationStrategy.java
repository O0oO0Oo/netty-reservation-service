package org.server.rsaga.saga.promise.strategy;

import io.netty.channel.EventLoop;
import io.netty.util.concurrent.Promise;
import org.server.rsaga.saga.api.SagaMessage;
import org.server.rsaga.saga.promise.SagaPromise;
import org.server.rsaga.saga.promise.impl.CompensableSagaPromise;
import org.server.rsaga.saga.step.impl.StepType;

import java.util.Map;
import java.util.function.Consumer;

public class CompensableSagaPromiseCreationStrategy implements SagaPromiseCreationStrategy {

    @Override
    public <I, R extends SagaMessage<?,?>> SagaPromise<I, R> createSagaPromise(Map<StepType, Consumer<I>> sagaSteps, EventLoop eventLoop) {
        Promise<R> executePromise = eventLoop.newPromise();
        return new CompensableSagaPromise<>(
                executePromise,
                sagaSteps.get(StepType.EXECUTE),
                sagaSteps.get(StepType.COMPENSATE)
        );
    }
    @Override
    public StepType getStepType() {
        return StepType.COMPENSATE;
    }
}