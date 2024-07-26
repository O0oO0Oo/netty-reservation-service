package org.server.rsaga.saga.api.factory;

import com.google.common.base.Preconditions;
import kotlin.jvm.functions.Function2;
import org.server.rsaga.saga.api.SagaDefinition;
import org.server.rsaga.saga.api.impl.DefaultSagaDefinition;
import org.server.rsaga.saga.message.MessageProducer;
import org.server.rsaga.saga.message.SagaMessage;
import org.server.rsaga.saga.promise.factory.SagaPromiseFactory;
import org.server.rsaga.saga.step.AggregateEventSagaStep;
import org.server.rsaga.saga.step.SingleEventSagaStep;
import org.server.rsaga.saga.step.impl.AggregateSagaStep;
import org.server.rsaga.saga.step.impl.SingleSagaStep;
import org.server.rsaga.saga.step.impl.StepType;

import java.util.*;
import java.util.function.Consumer;

public class SagaDefinitionFactoryImpl<I> {
    private static final int INIT_STEP_ID = 0;

    private int stepId = 1;
    private SagaPromiseFactory sagaPromiseFactory;
    private final TreeMap<Integer, Set<Integer>> dependencyMap;
    private final Map<String, Integer> stringSagaStepMap;
    private final Map<Integer, StepType> stepTypeMap;
    private final Map<Integer, Map<StepType, Consumer<SagaMessage<I>>>> sagaStepMap;
    private final Map<Integer, Map<StepType, Consumer<List<SagaMessage<I>>>>> aggregateSagaStepMap;

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
        sagaStepMap.putIfAbsent(INIT_STEP_ID, new EnumMap<>(StepType.class));
        sagaStepMap.get(INIT_STEP_ID).put(StepType.INITIAL, null);
    }

    public <R> SagaDefinitionFactoryImpl<I> addStep(String stepName, String destination, Function2<Integer, List<SagaMessage<I>>, SagaMessage<R>> operation, MessageProducer<R> messageProducer, StepType stepType, String... dependencies) {
        Integer existStepId = getExistStepId(stepName);
        int currentStepId = (existStepId != null) ? existStepId : stepId;

        checkStepAlreadyExist(stepName, stepType);

        AggregateEventSagaStep<I> sagaStep = new AggregateSagaStep<>(currentStepId, destination, operation, messageProducer, stepType);
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

    public <R> SagaDefinitionFactoryImpl<I> addStep(String stepName, String destination, Function2<Integer, SagaMessage<I>, SagaMessage<R>> operation, MessageProducer<R> messageProducer, StepType stepType, String dependency) {
        Integer existStepId = getExistStepId(stepName);
        int currentStepId = (existStepId != null) ? existStepId : stepId;

        checkStepAlreadyExist(stepName, stepType);

        SingleEventSagaStep<I> sagaStep = new SingleSagaStep<>(currentStepId, destination, operation, messageProducer, stepType);
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

    public <R> SagaDefinitionFactoryImpl<I> addStep(String stepName, String destination, Function2<Integer, SagaMessage<I>, SagaMessage<R>> operation, MessageProducer<R> messageProducer, StepType stepType) {
        Integer existStepId = getExistStepId(stepName);
        int currentStepId = (existStepId != null) ? existStepId : stepId;

        checkStepAlreadyExist(stepName, stepType);

        SingleEventSagaStep<I> sagaStep = new SingleSagaStep<>(currentStepId, destination, operation, messageProducer, stepType);
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

    public SagaDefinition<I> getSagaDefinition() {
        if (sagaPromiseFactory == null) {
            return new DefaultSagaDefinition<>(sagaStepMap, aggregateSagaStepMap, dependencyMap, stepTypeMap);
        }
        else {
            return new DefaultSagaDefinition<>(sagaPromiseFactory, sagaStepMap, aggregateSagaStepMap, dependencyMap, stepTypeMap);
        }
    }
}