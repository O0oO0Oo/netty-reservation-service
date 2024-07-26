package org.server.reservation.saga.promise.strategy;

import io.netty.channel.EventLoop;
import io.netty.util.concurrent.Promise;
import org.server.reservation.saga.promise.SagaPromise;
import org.server.reservation.saga.promise.impl.InitialSagaPromise;
import org.server.reservation.saga.step.impl.StepType;

import java.util.Map;
import java.util.function.Consumer;

public class InitialSagaPromiseCreationStrategy implements SagaPromiseCreationStrategy {
    @Override
    public <I, R> SagaPromise<I, R> createSagaPromise(Map<StepType, Consumer<I>> sagaSteps, EventLoop eventLoop) {
        Promise<R> executePromise = eventLoop.newPromise();
        return new InitialSagaPromise<>(executePromise);
    }

    @Override
    public StepType getStepType() {
        return StepType.INITIAL;
    }
}