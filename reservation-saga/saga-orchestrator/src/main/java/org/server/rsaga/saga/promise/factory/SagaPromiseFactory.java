package org.server.rsaga.saga.promise.factory;

import org.server.rsaga.saga.api.SagaMessage;
import org.server.rsaga.saga.promise.SagaPromise;
import org.server.rsaga.saga.step.impl.StepType;

import java.util.Map;
import java.util.function.Consumer;

public interface SagaPromiseFactory {
    <I, R extends SagaMessage<?, ?>> SagaPromise<I, R> createSagaPromise(StepType stepType, Map<StepType, Consumer<I>> sagaSteps);
}