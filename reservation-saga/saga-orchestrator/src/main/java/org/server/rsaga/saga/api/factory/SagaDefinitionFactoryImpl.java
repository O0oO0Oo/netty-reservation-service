package org.server.rsaga.saga.api.factory;

import com.google.common.base.Preconditions;
import org.server.rsaga.messaging.producer.MessageProducer;
import org.server.rsaga.saga.api.SagaDefinition;
import org.server.rsaga.saga.api.SagaMessage;
import org.server.rsaga.saga.api.impl.DefaultSagaDefinition;
import org.server.rsaga.saga.promise.factory.SagaPromiseFactory;
import org.server.rsaga.saga.step.AggregateEventSagaStep;
import org.server.rsaga.saga.step.SingleEventSagaStep;
import org.server.rsaga.saga.step.impl.AggregateSagaStep;
import org.server.rsaga.saga.step.impl.SingleSagaStep;
import org.server.rsaga.saga.step.impl.StepType;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;

public class SagaDefinitionFactoryImpl<K, V> implements SagaDefinitionFactory<K, V> {
    private static final int INIT_STEP_ID = 0;
    private static final String INITIAL_STEP_NAME = "INITIAL_STEP";

    private int stepId = 1;
    private SagaPromiseFactory sagaPromiseFactory;
    private final TreeMap<Integer, Set<Integer>> dependencyMap;
    private final Map<String, Integer> stringSagaStepMap;
    private final Map<Integer, StepType> stepTypeMap;
    private final Map<Integer, Map<StepType, Consumer<SagaMessage<K, V>>>> sagaStepMap;
    private final Map<Integer, Map<StepType, Consumer<List<SagaMessage<K, V>>>>> aggregateSagaStepMap;

    // todo : Refactoring
    public SagaDefinitionFactoryImpl() {
        this.dependencyMap = new TreeMap<>();
        this.stringSagaStepMap = new HashMap<>();
        this.stepTypeMap = new HashMap<>();
        this.sagaStepMap = new HashMap<>();
        this.aggregateSagaStepMap = new HashMap<>();
        init();
    }

    // 0번 실행 Step 설정
    private void init() {
        stringSagaStepMap.put(INITIAL_STEP_NAME, 0);
        sagaStepMap.putIfAbsent(INIT_STEP_ID, new EnumMap<>(StepType.class));
        sagaStepMap.get(INIT_STEP_ID).put(StepType.INITIAL, null);
    }

    public <P extends MessageProducer<K, V>> SagaDefinitionFactoryImpl<K, V> addStep(String stepName, String destination,
                                                   Function<List<SagaMessage<K, V>>, SagaMessage<K, V>> operation,
                                                   P messageProducer, StepType stepType, String... dependencies) {
        Integer existStepId = getExistStepId(stepName);
        int currentStepId = (existStepId != null) ? existStepId : stepId;

        checkStepAlreadyExist(stepName, stepType);

        AggregateEventSagaStep<K, V> sagaStep = new AggregateSagaStep<>(currentStepId, destination, operation, messageProducer, stepType);
        stringSagaStepMap.put(stepName, currentStepId);
        updateType(currentStepId, stepType);

        for (String dependency : dependencies) {
            checkDependencyExist(dependency);
            Integer dependencyId = stringSagaStepMap.get(dependency);
            putDependenciesWhenStepTypeExecute(currentStepId, dependencyId, stepType);
        }

        aggregateSagaStepMap.putIfAbsent(currentStepId, new EnumMap<>(StepType.class));
        aggregateSagaStepMap.get(currentStepId).put(stepType, sagaStep::publishEvent);

        if (existStepId == null) {
            stepId++;
        }
        return this;
    }

    public <P extends MessageProducer<K, V>> SagaDefinitionFactoryImpl<K, V> addStep(String stepName, String destination,
                                                UnaryOperator<SagaMessage<K, V>> operation,
                                                P messageProducer, StepType stepType, String dependency){
        Integer existStepId = getExistStepId(stepName);
        int currentStepId = (existStepId != null) ? existStepId : stepId;

        checkStepAlreadyExist(stepName, stepType);

        SingleEventSagaStep<K, V> sagaStep = new SingleSagaStep<>(currentStepId, destination, operation, messageProducer, stepType);
        stringSagaStepMap.put(stepName, currentStepId);
        updateType(currentStepId, stepType);

        checkDependencyExist(dependency);
        Integer dependencyId = stringSagaStepMap.get(dependency);
        putDependenciesWhenStepTypeExecute(currentStepId, dependencyId, stepType);

        sagaStepMap.putIfAbsent(currentStepId, new EnumMap<>(StepType.class));
        sagaStepMap.get(currentStepId).put(stepType, sagaStep::publishEvent);

        if (existStepId == null) {
            stepId++;
        }
        return this;
    }

    public <P extends MessageProducer<K, V>> SagaDefinitionFactoryImpl<K, V> addStep(String stepName, String destination,
                                                UnaryOperator<SagaMessage<K, V>> operation,
                                                P  messageProducer, StepType stepType){
        Integer existStepId = getExistStepId(stepName);
        int currentStepId = (existStepId != null) ? existStepId : stepId;

        checkStepAlreadyExist(stepName, stepType);

        SingleEventSagaStep<K, V> sagaStep = new SingleSagaStep<>(currentStepId, destination, operation, messageProducer, stepType);
        stringSagaStepMap.put(stepName, currentStepId);
        updateType(currentStepId, stepType);

        putDependenciesWhenStepTypeExecute(currentStepId, INIT_STEP_ID, stepType);

        sagaStepMap.putIfAbsent(currentStepId, new EnumMap<>(StepType.class));
        sagaStepMap.get(currentStepId).put(stepType, sagaStep::publishEvent);

        if (existStepId == null) {
            stepId++;
        }
        return this;
    }

    private Integer getExistStepId(String stepName) {
        if (stringSagaStepMap.containsKey(stepName)) {
            return stringSagaStepMap.get(stepName);
        }
        return null;
    }

    /**
     * StepName, StepType 둘 다 같은 Step 은 등록할 수 없다.
     */
    private void checkStepAlreadyExist(String stepName, StepType stepType) {
        if (stringSagaStepMap.containsKey(stepName)) {
            Integer id = stringSagaStepMap.get(stepName);

            boolean isStepExist = sagaStepMap.containsKey(id) && sagaStepMap.get(id).containsKey(stepType);
            boolean isAggregateStepExist = aggregateSagaStepMap.containsKey(id) && aggregateSagaStepMap.get(id).containsKey(stepType);

            Preconditions.checkArgument(!(isStepExist || isAggregateStepExist), "This step already exists.");
        }
    }

    private void updateType(Integer stepId, StepType stepType) {
        stepTypeMap.compute(stepId, (key, val) -> {
                    if (val == null) {
                        return stepType;
                    } else {
                        if (val == StepType.EXECUTE) {
                            return stepType;
                        }
                    }
                    return val;
                }
        );
    }

    private void checkDependencyExist(String dependency) {
        Preconditions.checkArgument(stringSagaStepMap.containsKey(dependency), "Saga steps must be added in execution order.");
    }

    private void putDependenciesWhenStepTypeExecute(Integer id, Integer dependencyId, StepType stepType) {
        if (stepType.equals(StepType.EXECUTE)) {
            dependencyMap.putIfAbsent(id, new HashSet<>());
            dependencyMap.get(id).add(dependencyId);
        }
    }

    public void setSagaPromiseFactory(SagaPromiseFactory sagaPromiseFactory) {
        this.sagaPromiseFactory = sagaPromiseFactory;
    }

    public SagaDefinition getSagaDefinition() {
        if (sagaPromiseFactory == null) {
            return new DefaultSagaDefinition<>(sagaStepMap, aggregateSagaStepMap, dependencyMap, stepTypeMap);
        }
        else {
            return new DefaultSagaDefinition<>(sagaPromiseFactory, sagaStepMap, aggregateSagaStepMap, dependencyMap, stepTypeMap);
        }
    }

    public static String getInitialStepName() {
        return INITIAL_STEP_NAME;
    }
}