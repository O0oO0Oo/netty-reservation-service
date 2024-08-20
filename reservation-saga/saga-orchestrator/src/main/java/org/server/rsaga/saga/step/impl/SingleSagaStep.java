package org.server.rsaga.saga.step.impl;


import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.server.rsaga.messaging.producer.MessageProducer;
import org.server.rsaga.saga.api.SagaMessage;
import org.server.rsaga.saga.step.SingleEventSagaStep;
import org.server.rsaga.saga.util.SagaMessageUtil;

import java.util.Map;
import java.util.function.UnaryOperator;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SingleSagaStep<K, V> implements SingleEventSagaStep<K, V> {
    private Integer stepId;
    private String destination;
    private UnaryOperator<SagaMessage<K, V>> operation;
    private MessageProducer<K, V> messageProducer;
    private StepType type;

    public SingleSagaStep(Integer stepId, String destination, UnaryOperator<SagaMessage<K, V>> operation, MessageProducer<K, V> messageProducer, StepType type) {
        this.stepId = stepId;
        this.destination = destination;
        this.operation = operation;
        this.messageProducer = messageProducer;
        this.type = type;
    }

    @Override
    public void publishEvent(SagaMessage<K, V> input) {
        SagaMessage<K, V> sagaMessage = this.operation.apply(input);
        buildMetadata(sagaMessage.metadata(), input);
        this.messageProducer.produce(destination, sagaMessage);
    }

    @Override
    public StepType getStepType() {
        return type;
    }

    private void buildMetadata(Map<String, byte[]> metadata, SagaMessage<K, V> input) {
        metadata.put(SagaMessage.STEP_TYPE, type.name().getBytes());
        metadata.put(SagaMessage.STEP_ID, SagaMessageUtil.intToByteArray(stepId));
        metadata.put(SagaMessage.CORRELATION_ID, SagaMessageUtil.tsidToBytes(
                input.correlationId()
        ));
    }
}