package org.server.rsaga.saga.api.factory;

import org.server.rsaga.messaging.producer.MessageProducer;
import org.server.rsaga.saga.api.SagaDefinition;
import org.server.rsaga.saga.api.SagaMessage;
import org.server.rsaga.saga.promise.factory.SagaPromiseFactory;
import org.server.rsaga.saga.step.impl.StepType;

import java.util.List;
import java.util.function.Function;
import java.util.function.UnaryOperator;

public interface SagaDefinitionFactory<K, V> {
    <P extends MessageProducer<K, V>> SagaDefinitionFactoryImpl<K, V> addStep(String stepName, String destination,
                                                                              Function<List<SagaMessage<K, V>>, SagaMessage<K, V>> operation,
                                                                              P messageProducer, StepType stepType, String... dependencies);

    <P extends MessageProducer<K, V>> SagaDefinitionFactoryImpl<K, V> addStep(String stepName, String destination,
                                                                              UnaryOperator<SagaMessage<K, V>> operation,
                                                                              P messageProducer, StepType stepType, String dependency);

    <P extends MessageProducer<K, V>> SagaDefinitionFactoryImpl<K, V> addStep(String stepName, String destination,
                                                                              UnaryOperator<SagaMessage<K, V>> operation,
                                                                              P messageProducer, StepType stepType);
    void setSagaPromiseFactory(SagaPromiseFactory sagaPromiseFactory);
    SagaDefinition getSagaDefinition();
}