package org.server.rsaga.saga.step.impl;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.server.rsaga.messaging.producer.MessageProducer;
import org.server.rsaga.saga.api.SagaMessage;
import org.server.rsaga.saga.step.AggregateEventSagaStep;
import org.server.rsaga.saga.util.SagaMessageUtil;

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AggregateSagaStep<K, V> implements AggregateEventSagaStep<K, V> {
    private Integer stepId;
    private String destination;
    private Function<List<SagaMessage<K, V>>, SagaMessage<K, V>> operation;
    private MessageProducer<K, V> messageProducer;
    private StepType type;

    public AggregateSagaStep(Integer stepId, String destination, Function<List<SagaMessage<K, V>>, SagaMessage<K, V>> operation, MessageProducer<K, V> messageProducer, StepType type) {
        this.stepId = stepId;
        this.destination = destination;
        this.operation = operation;
        this.messageProducer = messageProducer;
        this.type = type;
    }

    @Override
    public void publishEvent(List<SagaMessage<K, V>> input) {
        SagaMessage<K, V> sagaMessage = this.operation.apply(input);
        buildMetadata(sagaMessage.metadata(), input);
        this.messageProducer.produce(destination, sagaMessage);
    }

    @Override
    public StepType getStepType() {
        return type;
    }

    private void buildMetadata(Map<String, byte[]> metadata, List<SagaMessage<K, V>> input) {
        SagaMessage<K, V> inputMessage = input.get(0);

        metadata.put(SagaMessage.STEP_TYPE, type.name().getBytes());
        metadata.put(SagaMessage.STEP_ID, SagaMessageUtil.intToByteArray(stepId));
        metadata.put(SagaMessage.CORRELATION_ID, SagaMessageUtil.tsidToBytes(
                inputMessage.correlationId()
        ));
    }
}