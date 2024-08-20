package org.server.rsaga.saga.promise.strategy;

import io.netty.channel.EventLoop;
import org.server.rsaga.saga.api.SagaMessage;
import org.server.rsaga.saga.promise.SagaPromise;
import org.server.rsaga.saga.step.impl.StepType;

import java.util.Map;
import java.util.function.Consumer;

public interface SagaPromiseCreationStrategy {
    <I, R extends SagaMessage<?, ?>> SagaPromise<I, R> createSagaPromise(Map<StepType, Consumer<I>> sagaSteps, EventLoop eventLoop);
    StepType getStepType();
}