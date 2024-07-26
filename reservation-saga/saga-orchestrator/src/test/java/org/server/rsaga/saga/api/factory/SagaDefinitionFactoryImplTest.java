package org.server.rsaga.saga.api.factory;


import kotlin.jvm.functions.Function2;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.server.rsaga.saga.api.SagaDefinition;
import org.server.rsaga.saga.api.impl.DefaultSagaDefinition;
import org.server.rsaga.saga.message.MessageProducer;
import org.server.rsaga.saga.message.SagaMessage;
import org.server.rsaga.saga.promise.factory.SagaPromiseFactory;
import org.server.rsaga.saga.step.impl.AggregateSagaStep;
import org.server.rsaga.saga.step.impl.SingleSagaStep;
import org.server.rsaga.saga.step.impl.StepType;

import java.util.*;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

/**
 * <pre>
 * {@link SagaDefinitionFactoryImpl} 의 테스트이다.
 * 테스트에서 생성된 SagaDefinition 의 내부 상태 검증은 equals 로 비교한다.
 * </pre>
 */
@ExtendWith(MockitoExtension.class)
class SagaDefinitionFactoryImplTest {

    @Mock
    MessageProducer<Integer> messageProducer;

    @Mock
    SagaPromiseFactory sagaPromiseFactory;

    @InjectMocks
    SagaDefinitionFactoryImpl<Integer> sagaDefinitionFactory;


    /**
     *<pre>
     *<code>
     *
     *          +-> [1] --+
     *          |         | ------------> [6] --------> [7]
     *          |    +----+                ^
     *          |    |                     |
     * [0] ---> +-> [2]                    |
     *          |    |                     |
     *          |    +----+                |
     *          |         | ---> [4] ---> [5 (Compensable)]
     *          +-> [3] --+
     *
     *</code>
     *</pre>
     */
    @Test
    @DisplayName("올바른 복잡한 단계 - 설정 - 성공")
    void should_sagaDefinitionCreated_when_validStepsAndOrder() {
        // given
        @SuppressWarnings("unchecked")
        Function2<Integer, SagaMessage<Integer>, SagaMessage<Integer>> operation = Mockito.spy(Function2.class);
        @SuppressWarnings("unchecked")
        Function2<Integer, List<SagaMessage<Integer>>, SagaMessage<Integer>> aggregateOperation = Mockito.mock(Function2.class);

        SingleSagaStep<Integer, Integer> step1 = new SingleSagaStep<>(1, "none", operation, messageProducer, StepType.EXECUTE);
        SingleSagaStep<Integer, Integer> step2 = new SingleSagaStep<>(2, "none", operation, messageProducer, StepType.EXECUTE);
        SingleSagaStep<Integer, Integer> step3 = new SingleSagaStep<>(3, "none", operation, messageProducer, StepType.EXECUTE);
        AggregateSagaStep<Integer, Integer> step4 = new AggregateSagaStep<>(4, "none", aggregateOperation, messageProducer, StepType.EXECUTE);
        SingleSagaStep<Integer, Integer> step5 = new SingleSagaStep<>(5, "none", operation, messageProducer, StepType.EXECUTE);
        SingleSagaStep<Integer, Integer> step5Compensate = new SingleSagaStep<>(5, "none", operation, messageProducer, StepType.COMPENSATE);
        AggregateSagaStep<Integer, Integer> step6 = new AggregateSagaStep<>(6, "none", aggregateOperation, messageProducer, StepType.EXECUTE);
        SingleSagaStep<Integer, Integer> step7 = new SingleSagaStep<>(6, "none", operation, messageProducer, StepType.EXECUTE);

        Map<Integer, Map<StepType, Consumer<SagaMessage<Integer>>>> sagaStepMap = new HashMap<>();
        sagaStepMap.putIfAbsent(0, new EnumMap<>(StepType.class));
        sagaStepMap.get(0).put(StepType.INITIAL, null);
        sagaStepMap.putIfAbsent(1, new EnumMap<>(StepType.class));
        sagaStepMap.get(1).put(StepType.EXECUTE, step1::publishEvent);
        sagaStepMap.putIfAbsent(2, new EnumMap<>(StepType.class));
        sagaStepMap.get(2).put(StepType.EXECUTE, step2::publishEvent);
        sagaStepMap.putIfAbsent(3, new EnumMap<>(StepType.class));
        sagaStepMap.get(3).put(StepType.EXECUTE, step3::publishEvent);
        sagaStepMap.putIfAbsent(5, new EnumMap<>(StepType.class));
        sagaStepMap.get(5).put(StepType.EXECUTE, step5::publishEvent);
        sagaStepMap.get(5).put(StepType.COMPENSATE, step5Compensate::publishEvent);
        sagaStepMap.putIfAbsent(7, new EnumMap<>(StepType.class));
        sagaStepMap.get(7).put(StepType.EXECUTE, step7::publishEvent);

        Map<Integer, Map<StepType, Consumer<List<SagaMessage<Integer>>>>> aggregateSagaStepMap = new HashMap<>();
        aggregateSagaStepMap.putIfAbsent(4, new EnumMap<>(StepType.class));
        aggregateSagaStepMap.get(4).put(StepType.EXECUTE, step4::publishEvent);
        aggregateSagaStepMap.putIfAbsent(6, new EnumMap<>(StepType.class));
        aggregateSagaStepMap.get(6).put(StepType.EXECUTE, step6::publishEvent);

        SortedMap<Integer, Set<Integer>> dependencyMap = new TreeMap<>();
        dependencyMap.putIfAbsent(1, new HashSet<>());
        dependencyMap.get(1).add(0);
        dependencyMap.putIfAbsent(2, new HashSet<>());
        dependencyMap.get(2).add(0);
        dependencyMap.putIfAbsent(3, new HashSet<>());
        dependencyMap.get(3).add(0);
        dependencyMap.putIfAbsent(4, new HashSet<>());
        dependencyMap.get(4).add(2);
        dependencyMap.get(4).add(3);
        dependencyMap.putIfAbsent(5, new HashSet<>());
        dependencyMap.get(5).add(4);
        dependencyMap.putIfAbsent(6, new HashSet<>());
        dependencyMap.get(6).add(1);
        dependencyMap.get(6).add(2);
        dependencyMap.get(6).add(5);
        dependencyMap.putIfAbsent(7, new HashSet<>());
        dependencyMap.get(7).add(6);

        Map<Integer, StepType> stepTypeMap = new HashMap<>();
        stepTypeMap.put(1, StepType.EXECUTE);
        stepTypeMap.put(2, StepType.EXECUTE);
        stepTypeMap.put(3, StepType.EXECUTE);
        stepTypeMap.put(4, StepType.EXECUTE);
        stepTypeMap.put(5, StepType.COMPENSATE);
        stepTypeMap.put(6, StepType.EXECUTE);
        stepTypeMap.put(7, StepType.EXECUTE);

        DefaultSagaDefinition<Integer> expectSagaDefinition = new DefaultSagaDefinition<>(sagaPromiseFactory, sagaStepMap, aggregateSagaStepMap, dependencyMap, stepTypeMap);

        // when
        SagaDefinition<Integer> actureSagaDefinition = sagaDefinitionFactory
                .addStep("step1", "none", operation, messageProducer, StepType.EXECUTE)
                .addStep("step2", "none", operation, messageProducer, StepType.EXECUTE)
                .addStep("step3", "none", operation, messageProducer, StepType.EXECUTE)
                .addStep("step4", "none", aggregateOperation, messageProducer, StepType.EXECUTE, "step2", "step3")
                .addStep("step5", "none", operation, messageProducer, StepType.EXECUTE, "step4")
                .addStep("step5", "none", operation, messageProducer, StepType.COMPENSATE)
                .addStep("step6", "none", aggregateOperation, messageProducer, StepType.EXECUTE, "step1", "step2", "step5")
                .addStep("step7", "none", operation, messageProducer, StepType.EXECUTE, "step6")
                .getSagaDefinition();

        //then
        assertNotNull(actureSagaDefinition, "The created SagaDefinition is null.");
        assertEquals(expectSagaDefinition, actureSagaDefinition, "The created SagaDefinition and the expected SagaDefinition are different.");
    }

    /**
     * <pre>
     *     <core>
     *         1. [init] -> [1] 등록
     *         2. [2] -> [3] 등록 하지만 2는 아직 등록하지 않았음, 예외 발생
     *         3. [init] -> [2] 등록
     *     </core>
     * </pre>
     */
    @Test
    @DisplayName("잘못된 등록 순서 - 단계 등록 - 예외 발생")
    void should_fail_when_stepsOutOfOrder() {
        // given
        @SuppressWarnings("unchecked")
        Function2<Integer, SagaMessage<Integer>, SagaMessage<Integer>> operation = Mockito.mock(Function2.class);
        @SuppressWarnings("unchecked")
        Function2<Integer, List<SagaMessage<Integer>>, SagaMessage<Integer>> aggregateOperation =  Mockito.mock(Function2.class);

        // when
        sagaDefinitionFactory
                .addStep("step1", "none", operation, messageProducer, StepType.EXECUTE);

        // 잘못된 순서.
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            sagaDefinitionFactory
                    .addStep("step3", "none", operation, messageProducer, StepType.EXECUTE, "step2");
        });

        sagaDefinitionFactory
                .addStep("step2", "none", operation, messageProducer, StepType.EXECUTE, "step1");

        // then
        assertEquals("Saga steps must be added in execution order.", exception.getMessage());
    }

    @Test
    @DisplayName("동일한 이름, 타입의 step - 등록 - 예외 발생")
    void should_fail_when_addIdenticalNameAndTypeSteps() {
        // given
        @SuppressWarnings("unchecked")
        Function2<Integer, SagaMessage<Integer>, SagaMessage<Integer>> operation = Mockito.mock(Function2.class);
        @SuppressWarnings("unchecked")
        Function2<Integer, List<SagaMessage<Integer>>, SagaMessage<Integer>> aggregateOperation =  Mockito.mock(Function2.class);


        // when
        sagaDefinitionFactory
                .addStep("step0", "none", operation, messageProducer, StepType.EXECUTE)
                .addStep("step1", "none", operation, messageProducer, StepType.EXECUTE, "step0");

        IllegalArgumentException exceptionWhenAddSameExecuteStep = assertThrows(IllegalArgumentException.class, () -> {
            sagaDefinitionFactory
                    .addStep("step1", "none", operation, messageProducer, StepType.EXECUTE);
        });

        sagaDefinitionFactory
                .addStep("step2", "none", operation, messageProducer, StepType.EXECUTE, "step1")
                .addStep("step2", "none", operation, messageProducer, StepType.COMPENSATE, "step1");

        IllegalArgumentException exceptionWhenAddSameCompensateStep = assertThrows(IllegalArgumentException.class, () -> {
            sagaDefinitionFactory
                    .addStep("step2", "none", operation, messageProducer, StepType.COMPENSATE, "step1");
        });

        IllegalArgumentException exceptionWhenAddSameCompensateStepAndDiffDependency = assertThrows(IllegalArgumentException.class, () -> {
            sagaDefinitionFactory
                    .addStep("step2", "none", operation, messageProducer, StepType.COMPENSATE, "step0");
        });


        // then
        assertEquals("This step already exists.", exceptionWhenAddSameExecuteStep.getMessage());
        assertEquals("This step already exists.", exceptionWhenAddSameCompensateStep.getMessage());
        assertEquals("This step already exists.", exceptionWhenAddSameCompensateStepAndDiffDependency.getMessage());
    }

    @Test
    @DisplayName("올바른 순서의 execute step - 이후 execute type 이 아닌 step 기능 추가 등록 - 성공")
    void should_sagaDefinitionCreated_when_addNonExecuteStepAfterValidExecuteSteps() {
        // given
        @SuppressWarnings("unchecked")
        Function2<Integer, SagaMessage<Integer>, SagaMessage<Integer>> operation = Mockito.mock(Function2.class);
        @SuppressWarnings("unchecked")
        Function2<Integer, List<SagaMessage<Integer>>, SagaMessage<Integer>> aggregateOperation =  Mockito.mock(Function2.class);

        SingleSagaStep<Integer, Integer> step1 = new SingleSagaStep<>(1, "none", operation, messageProducer, StepType.EXECUTE);
        SingleSagaStep<Integer, Integer> step2 = new SingleSagaStep<>(2, "none", operation, messageProducer, StepType.EXECUTE);
        SingleSagaStep<Integer, Integer> step3 = new SingleSagaStep<>(3, "none", operation, messageProducer, StepType.EXECUTE);
        SingleSagaStep<Integer, Integer> step3Compensate = new SingleSagaStep<>(3, "none", operation, messageProducer, StepType.COMPENSATE);
        AggregateSagaStep<Integer, Integer> step4 = new AggregateSagaStep<>(4, "none", aggregateOperation, messageProducer, StepType.EXECUTE);
        AggregateSagaStep<Integer, Integer> step5 = new AggregateSagaStep<>(5, "none", aggregateOperation, messageProducer, StepType.EXECUTE);

        Map<Integer, Map<StepType, Consumer<SagaMessage<Integer>>>> sagaStepMap = new HashMap<>();
        sagaStepMap.putIfAbsent(0, new EnumMap<>(StepType.class));
        sagaStepMap.get(0).put(StepType.INITIAL, null);
        sagaStepMap.putIfAbsent(1, new EnumMap<>(StepType.class));
        sagaStepMap.get(1).put(StepType.EXECUTE, step1::publishEvent);
        sagaStepMap.putIfAbsent(2, new EnumMap<>(StepType.class));
        sagaStepMap.get(2).put(StepType.EXECUTE, step2::publishEvent);
        sagaStepMap.putIfAbsent(3, new EnumMap<>(StepType.class));
        sagaStepMap.get(3).put(StepType.EXECUTE, step3::publishEvent);
        sagaStepMap.get(3).put(StepType.COMPENSATE, step3Compensate::publishEvent);

        Map<Integer, Map<StepType, Consumer<List<SagaMessage<Integer>>>>> aggregateSagaStepMap = new HashMap<>();
        aggregateSagaStepMap.putIfAbsent(4, new EnumMap<>(StepType.class));
        aggregateSagaStepMap.get(4).put(StepType.EXECUTE, step4::publishEvent);
        aggregateSagaStepMap.putIfAbsent(5, new EnumMap<>(StepType.class));
        aggregateSagaStepMap.get(5).put(StepType.EXECUTE, step5::publishEvent);

        SortedMap<Integer, Set<Integer>> dependencyMap = new TreeMap<>();
        dependencyMap.putIfAbsent(1, new HashSet<>());
        dependencyMap.get(1).add(0);
        dependencyMap.putIfAbsent(2, new HashSet<>());
        dependencyMap.get(2).add(0);
        dependencyMap.putIfAbsent(3, new HashSet<>());
        dependencyMap.get(3).add(0);
        dependencyMap.putIfAbsent(4, new HashSet<>());
        dependencyMap.get(4).add(2);
        dependencyMap.get(4).add(3);
        dependencyMap.putIfAbsent(5, new HashSet<>());
        dependencyMap.get(5).add(1);
        dependencyMap.get(5).add(4);

        Map<Integer, StepType> stepTypeMap = new HashMap<>();
        stepTypeMap.put(1, StepType.EXECUTE);
        stepTypeMap.put(2, StepType.EXECUTE);
        stepTypeMap.put(3, StepType.COMPENSATE);
        stepTypeMap.put(4, StepType.EXECUTE);
        stepTypeMap.put(5, StepType.EXECUTE);

        DefaultSagaDefinition<Integer> expectSagaDefinition = new DefaultSagaDefinition<>(sagaPromiseFactory, sagaStepMap, aggregateSagaStepMap, dependencyMap, stepTypeMap);

        // when
        SagaDefinition<Integer> actureSagaDefinition = sagaDefinitionFactory
                .addStep("step1", "none", operation, messageProducer, StepType.EXECUTE)
                .addStep("step2", "none", operation, messageProducer, StepType.EXECUTE)
                .addStep("step3", "none", operation, messageProducer, StepType.EXECUTE)
                .addStep("step4", "none", aggregateOperation, messageProducer, StepType.EXECUTE, "step2", "step3")
                .addStep("step5", "none", aggregateOperation, messageProducer, StepType.EXECUTE, "step1", "step4")
                .addStep("step3", "none", operation, messageProducer, StepType.COMPENSATE)
                .getSagaDefinition();

        //then
        assertNotNull(actureSagaDefinition, "The created SagaDefinition is null.");
        assertEquals(expectSagaDefinition, actureSagaDefinition, "The created SagaDefinition and the expected SagaDefinition are different.");
    }
}