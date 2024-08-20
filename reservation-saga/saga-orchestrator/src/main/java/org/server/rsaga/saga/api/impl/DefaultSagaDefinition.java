package org.server.rsaga.saga.api.impl;

import org.server.rsaga.saga.api.SagaDefinition;
import org.server.rsaga.saga.api.SagaMessage;
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

public class DefaultSagaDefinition<K, V> implements SagaDefinition {
    private static final int INIT_STEP_ID = 0;

    private final SagaPromiseFactory sagaPromiseFactory;
    private final Map<Integer, Map<StepType, Consumer<SagaMessage<K, V>>>> sagaStepMap;
    private final Map<Integer, Map<StepType, Consumer<List<SagaMessage<K, V>>>>> aggregateSagaStepMap;
    private final SortedMap<Integer, Set<Integer>> dependencyMap;
    private final Map<Integer, StepType> stepTypeMap;

    public DefaultSagaDefinition(Map<Integer, Map<StepType, Consumer<SagaMessage<K, V>>>> sagaStepMap,
                                 Map<Integer, Map<StepType, Consumer<List<SagaMessage<K, V>>>>> aggregateSagaStepMap,
                                 SortedMap<Integer, Set<Integer>> dependencyMap,
                                 Map<Integer, StepType> stepTypeMap) {
        this(new SagaPromiseFactoryImpl(), sagaStepMap, aggregateSagaStepMap, dependencyMap, stepTypeMap);
    }

    public DefaultSagaDefinition(SagaPromiseFactory sagaPromiseFactory,
                                 Map<Integer, Map<StepType, Consumer<SagaMessage<K, V>>>> sagaStepMap,
                                 Map<Integer, Map<StepType, Consumer<List<SagaMessage<K, V>>>>> aggregateSagaStepMap,
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
    public SagaPromise<?, SagaMessage<K, V>>[] initializeSaga() {
        SagaPromise<?, SagaMessage<K, V>>[] sagaPromises = new SagaPromise[dependencyMap.size() + 1];
        sagaPromises[INIT_STEP_ID] = createInitialSagaPromise();

        for (Map.Entry<Integer, Set<Integer>> dependencyEntry : dependencyMap.entrySet()) {
            Integer id = dependencyEntry.getKey();
            Set<Integer> dependencies = dependencyEntry.getValue();

            sagaPromises[id] = creataSagaPromise(id, dependencies, sagaPromises);
        }

        return sagaPromises;
    }

    private SagaPromise<?, SagaMessage<K, V>> createInitialSagaPromise() {
        return sagaPromiseFactory.createSagaPromise(StepType.INITIAL, sagaStepMap.get(0));
    }

    private SagaPromise<?, SagaMessage<K, V>> creataSagaPromise(Integer id, Set<Integer> dependencies, SagaPromise<?, SagaMessage<K, V>>[] sagaPromises) {
        StepType stepType = stepTypeMap.get(id);

        if (dependencies.size() == 1) {
            return createSingleDependencySagaPromise(id, dependencies, sagaPromises, stepType);
        }
        else {
            return createMultipleDependenciesSagaPromise(id, dependencies, sagaPromises, stepType);
        }
    }

    private SagaPromise<?, SagaMessage<K, V>> createSingleDependencySagaPromise(Integer id, Set<Integer> dependencies, SagaPromise<?, SagaMessage<K, V>>[] sagaPromises, StepType stepType) {
        SagaPromise<SagaMessage<K, V>, SagaMessage<K, V>> sagaPromise = sagaPromiseFactory.createSagaPromise(stepType, sagaStepMap.get(id));

        Integer dependencyId = dependencies.iterator().next();
        SagaPromise<?, SagaMessage<K, V>> dependencySagaPromise = sagaPromises[dependencyId];

        dependencySagaPromise.addListener(sagaPromise);
        return sagaPromise;
    }

    private SagaPromise<?, SagaMessage<K, V>> createMultipleDependenciesSagaPromise(Integer id, Set<Integer> dependencies, SagaPromise<?, SagaMessage<K, V>>[] sagaPromises, StepType stepType) {
        SagaPromiseAggregator<SagaMessage<K, V>, SagaMessage<K, V>> sagaPromiseAggregator = new SagaPromiseAggregator<>();

        for (Integer dependencyId : dependencies) {
            SagaPromise<?, SagaMessage<K, V>> dependencySagaPromise = sagaPromises[dependencyId];
            sagaPromiseAggregator.add(dependencySagaPromise);
        }

        SagaPromise<List<SagaMessage<K, V>>, SagaMessage<K, V>> aggregateSagaPromise = sagaPromiseFactory.createSagaPromise(stepType, aggregateSagaStepMap.get(id));
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

        DefaultSagaDefinition<K, V> that;
        try {
            that = (DefaultSagaDefinition<K, V>) o;
        } catch (ClassCastException e) {
            return false;
        }

        return sagaPromiseFactory.equals(that.sagaPromiseFactory) &&
                compareStepMaps(sagaStepMap, that.sagaStepMap) &&
                compareStepMaps(aggregateSagaStepMap, that.aggregateSagaStepMap) &&
                dependencyMap.equals(that.dependencyMap) &&
                stepTypeMap.equals(that.stepTypeMap);
    }

    private <T> boolean compareStepMaps(Map<Integer, Map<StepType, T>> map1, Map<Integer, Map<StepType, T>> map2) {
        if (!map1.keySet().equals(map2.keySet())) {
            return false;
        }

        for (Map.Entry<Integer, Map<StepType, T>> map1Entry : map1.entrySet()) {
            Integer key = map1Entry.getKey();
            Map<StepType, T> value = map1Entry.getValue();
            if (!value.keySet().equals(map2.get(key).keySet())) {
                return false;
            }
        }

        return true;
    }}