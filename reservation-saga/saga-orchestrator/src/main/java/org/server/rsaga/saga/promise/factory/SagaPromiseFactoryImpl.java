package org.server.rsaga.saga.promise.factory;

import com.google.common.base.Preconditions;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.DefaultExecutorServiceFactory;
import io.netty.util.concurrent.EventExecutorGroup;
import org.server.rsaga.saga.api.SagaMessage;
import org.server.rsaga.saga.promise.SagaPromise;
import org.server.rsaga.saga.promise.strategy.SagaPromiseCreationStrategy;
import org.server.rsaga.saga.step.impl.StepType;

import java.util.EnumMap;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.function.Consumer;

/**
 * <pre>
 * {@link ServiceLoader} 를 사용하여 동적으로 {@link StepType} 에 따라, 그에 맞는 {@link SagaPromise} 를 생성하는 팩토리 클래스이다.
 * {@link StepType} 에 대한 적절한 {@link SagaPromiseCreationStrategy} 를 호출한다.
 * </pre>
 */
public class SagaPromiseFactoryImpl implements SagaPromiseFactory{
    private final Map<StepType, SagaPromiseCreationStrategy> strategies;
    private final EventExecutorGroup eventExecutorGroup;

    public SagaPromiseFactoryImpl() {
        this(new DefaultEventExecutorGroup(
                        2,
                        new DefaultExecutorServiceFactory("saga-thread-group")
                )
        );
    }

    public SagaPromiseFactoryImpl(EventExecutorGroup eventExecutorGroup) {
        this.eventExecutorGroup = eventExecutorGroup;
        this.strategies = new EnumMap<>(StepType.class);
        ServiceLoader<SagaPromiseCreationStrategy> loader = ServiceLoader.load(SagaPromiseCreationStrategy.class);
        for (SagaPromiseCreationStrategy strategy : loader) {
            this.strategies.put(strategy.getStepType(), strategy);
        }
    }

    @Override
    public <I, R extends SagaMessage<?, ?>> SagaPromise<I, R> createSagaPromise(StepType stepType, Map<StepType, Consumer<I>> sagaSteps) {
        SagaPromiseCreationStrategy strategy = getStrategy(stepType);
        return strategy.createSagaPromise(sagaSteps, eventExecutorGroup.next());
    }

    /**
     * @return {@link StepType} 에 맞는 생성 전략을 반환한다.
     * @throws IllegalArgumentException {@link StepType} 에 맞는 {@link SagaPromiseCreationStrategy} 가 없을 때.
     */
    private SagaPromiseCreationStrategy getStrategy(StepType stepType) {
        Preconditions.checkArgument(
                strategies.containsKey(stepType),
                "This step type does not exist. implement saga creation strategy for this step type. : %s", stepType.name());
        return strategies.get(stepType);
    }
}