package org.server.rsaga.saga.step.impl;


import kotlin.jvm.functions.Function2;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.server.rsaga.saga.message.MessageProducer;
import org.server.rsaga.saga.message.SagaMessage;
import org.server.rsaga.saga.step.SingleEventSagaStep;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SingleSagaStep<I, R> implements SingleEventSagaStep<I> {
    private Integer stepId;
    private String destination;
    private Function2<Integer, SagaMessage<I>, SagaMessage<R>> operation;
    private MessageProducer<R> messageProducer;
    private StepType type;

    public SingleSagaStep(Integer stepId, String destination, Function2<Integer, SagaMessage<I>, SagaMessage<R>> operation, MessageProducer<R> messageProducer, StepType type) {
        this.stepId = stepId;
        this.destination = destination;
        this.operation = operation;
        this.messageProducer = messageProducer;
        this.type = type;
    }

    @Override
    public void publishEvent(SagaMessage<I> input) {
        SagaMessage<R> sagaMessage = this.operation.invoke(stepId, input);
        this.messageProducer.produce(destination, sagaMessage);
    }

    @Override
    public StepType getStepType() {
        return type;
    }
}