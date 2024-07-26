package org.server.rsaga.saga.step.impl;

import kotlin.jvm.functions.Function2;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.server.rsaga.saga.message.MessageProducer;
import org.server.rsaga.saga.message.SagaMessage;
import org.server.rsaga.saga.step.AggregateEventSagaStep;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AggregateSagaStep<I, R> implements AggregateEventSagaStep<I> {
    private Integer stepId;
    private String destination;
    private Function2<Integer, List<SagaMessage<I>>, SagaMessage<R>> operation;
    private MessageProducer<R> messageProducer;
    private StepType type;

    public AggregateSagaStep(Integer stepId, String destination, Function2<Integer, List<SagaMessage<I>>, SagaMessage<R>> operation, MessageProducer<R> messageProducer, StepType type) {
        this.stepId = stepId;
        this.destination = destination;
        this.operation = operation;
        this.messageProducer = messageProducer;
        this.type = type;
    }

    @Override
    public void publishEvent(List<SagaMessage<I>> input) {
        SagaMessage<R> sagaMessage = this.operation.invoke(stepId, input);
        this.messageProducer.produce(destination, sagaMessage);
    }

    @Override
    public StepType getStepType() {
        return type;
    }
}