package org.server.reservation.saga.promise.strategy;

import io.netty.channel.EventLoop;
import org.server.reservation.saga.promise.SagaPromise;
import org.server.reservation.saga.step.impl.StepType;

import java.util.Map;
import java.util.function.Consumer;

public interface SagaPromiseCreationStrategy {
    <I, R> SagaPromise<I, R> createSagaPromise(Map<StepType, Consumer<I>> sagaSteps, EventLoop eventLoop);
    StepType getStepType();
}