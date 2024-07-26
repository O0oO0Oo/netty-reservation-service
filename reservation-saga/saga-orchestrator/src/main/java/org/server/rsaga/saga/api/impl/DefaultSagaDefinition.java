package org.server.rsaga.saga.api.impl;

import org.server.rsaga.saga.api.SagaDefinition;
import org.server.rsaga.saga.message.SagaMessage;
import org.server.rsaga.saga.promise.SagaPromise;
import org.server.rsaga.saga.promise.factory.SagaPromiseFactory;
import org.server.rsaga.saga.promise.factory.SagaPromiseFactoryImpl;
import org.server.rsaga.saga.promise.impl.SagaPromiseAggregator;
import org.server.rsaga.saga.step.impl.StepType;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.function.Consumer;

public class DefaultSagaDefinition<T> implements SagaDefinition<T> {
    private static final int INIT_STEP_ID = 0;

    private final SagaPromiseFactory sagaPromiseFactory;
    private final Map<Integer, Map<StepType, Consumer<SagaMessage<T>>>> sagaStepMap;
    private final Map<Integer, Map<StepType, Consumer<List<SagaMessage<T>>>>> aggregateSagaStepMap;
    private final SortedMap<Integer, Set<Integer>> dependencyMap;
    private final Map<Integer, StepType> stepTypeMap;

    public DefaultSagaDefinition(Map<Integer, Map<StepType, Consumer<SagaMessage<T>>>> sagaStepMap,
                                 Map<Integer, Map<StepType, Consumer<List<SagaMessage<T>>>>> aggregateSagaStepMap,
                                 SortedMap<Integer, Set<Integer>> dependencyMap,
                                 Map<Integer, StepType> stepTypeMap) {
        this(new SagaPromiseFactoryImpl(), sagaStepMap, aggregateSagaStepMap, dependencyMap, stepTypeMap);
    }

    public DefaultSagaDefinition(SagaPromiseFactory sagaPromiseFactory,
                                 Map<Integer, Map<StepType, Consumer<SagaMessage<T>>>> sagaStepMap,
                                 Map<Integer, Map<StepType, Consumer<List<SagaMessage<T>>>>> aggregateSagaStepMap,
                                 SortedMap<Integer, Set<Integer>> dependencyMap,
                                 Map<Integer, StepType> stepTypeMap) {
        this.sagaPromiseFactory = sagaPromiseFactory;
        this.sagaStepMap = sagaStepMap;
        this.aggregateSagaStepMap = aggregateSagaStepMap;
        this.dependencyMap = dependencyMap;
        this.stepTypeMap = stepTypeMap;
    }

    @Override
    @SuppressWarnings("unchecked")
    public SagaPromise<?, SagaMessage<T>>[] initializeSaga() {
        SagaPromise<?, SagaMessage<T>>[] sagaPromises = new SagaPromise[dependencyMap.size() + 1];
        sagaPromises[INIT_STEP_ID] = createInitialSagaPromise();

        for (Map.Entry<Integer, Set<Integer>> dependencyEntry : dependencyMap.entrySet()) {
            Integer id = dependencyEntry.getKey();
            Set<Integer> dependencies = dependencyEntry.getValue();

            sagaPromises[id] = creataSagaPromise(id, dependencies, sagaPromises);
        }

        return sagaPromises;
    }

    private SagaPromise<?, SagaMessage<T>> createInitialSagaPromise() {
        return sagaPromiseFactory.createSagaPromise(StepType.INITIAL, sagaStepMap.get(0));
    }

    private SagaPromise<?, SagaMessage<T>> creataSagaPromise(Integer id, Set<Integer> dependencies, SagaPromise<?, SagaMessage<T>>[] sagaPromises) {
        StepType stepType = stepTypeMap.get(id);

        if (dependencies.size() == 1) {
            return createSingleDependencySagaPromise(id, dependencies, sagaPromises, stepType);
        }
        else {
            return createMultipleDependenciesSagaPromise(id, dependencies, sagaPromises, stepType);
        }
    }

    private SagaPromise<?, SagaMessage<T>> createSingleDependencySagaPromise(Integer id, Set<Integer> dependencies, SagaPromise<?, SagaMessage<T>>[] sagaPromises, StepType stepType) {
        SagaPromise<SagaMessage<T>, SagaMessage<T>> sagaPromise = sagaPromiseFactory.createSagaPromise(stepType, sagaStepMap.get(id));

        Integer dependencyId = dependencies.iterator().next();
        SagaPromise<?, SagaMessage<T>> dependencySagaPromise = sagaPromises[dependencyId];

        dependencySagaPromise.addListener(sagaPromise);
        return sagaPromise;
    }

    private SagaPromise<?, SagaMessage<T>> createMultipleDependenciesSagaPromise(Integer id, Set<Integer> dependencies, SagaPromise<?, SagaMessage<T>>[] sagaPromises, StepType stepType) {
        SagaPromiseAggregator<SagaMessage<T>, SagaMessage<T>> sagaPromiseAggregator = new SagaPromiseAggregator<>();

        for (Integer dependencyId : dependencies) {
            SagaPromise<?, SagaMessage<T>> dependencySagaPromise = sagaPromises[dependencyId];
            sagaPromiseAggregator.add(dependencySagaPromise);
        }

        SagaPromise<List<SagaMessage<T>>, SagaMessage<T>> aggregateSagaPromise = sagaPromiseFactory.createSagaPromise(stepType, aggregateSagaStepMap.get(id));
        sagaPromiseAggregator.finish(aggregateSagaPromise);
        return aggregateSagaPromise;
    }

    /**
     * 테스트를 위한 equals, Consumer 는 동등성 비교가 어렵다.
     * 따라서 현재는 KeySet 만 비교를 하였다.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DefaultSagaDefinition<T> that;
        try {
            that = (DefaultSagaDefinition<T>) o;
        } catch (ClassCastException e) {
            return false;
        }

        return sagaPromiseFactory.equals(that.sagaPromiseFactory) &&
                compareStepMaps(sagaStepMap, that.sagaStepMap) &&
                compareStepMaps(aggregateSagaStepMap, that.aggregateSagaStepMap) &&
                dependencyMap.equals(that.dependencyMap) &&
                stepTypeMap.equals(that.stepTypeMap);
    }

    private <V> boolean compareStepMaps(Map<Integer, Map<StepType, V>> map1, Map<Integer, Map<StepType, V>> map2) {
        if (!map1.keySet().equals(map2.keySet())) {
            return false;
        }

        for (Map.Entry<Integer, Map<StepType, V>> map1Entry : map1.entrySet()) {
            Integer key = map1Entry.getKey();
            Map<StepType, V> value = map1Entry.getValue();
            if (!value.keySet().equals(map2.get(key).keySet())) {
                return false;
            }
        }

        return true;
    }}