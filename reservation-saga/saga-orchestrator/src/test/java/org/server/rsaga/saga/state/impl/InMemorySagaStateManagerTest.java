package org.server.rsaga.saga.state.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.server.rsaga.saga.api.SagaDefinition;
import org.server.rsaga.saga.message.Key;
import org.server.rsaga.saga.message.SagaMessage;
import org.server.rsaga.saga.promise.SagaPromise;
import org.server.rsaga.saga.state.SagaState;
import org.server.rsaga.saga.state.SagaStateCache;
import org.server.rsaga.saga.state.factory.SagaStateFactory;

import static org.mockito.Mockito.*;
/**
 * <pre>
 * 사가의 상태를 관리하는 {@link org.server.rsaga.saga.state.SagaStateManager} 의 테스트
 * </pre>
 */
@ExtendWith(MockitoExtension.class)
class InMemorySagaStateManagerTest {

    @Mock
    SagaDefinition<Integer> sagaDefinition;
    @Mock
    SagaStateFactory<Integer> sagaStateFactory;
    @Mock
    SagaStateCache<Integer> cache;
    @Mock
    SagaMessage<Integer> sagaMessage;
    @Mock
    SagaState<Integer> sagaState;
    @Mock
    Key correlationId;

    @InjectMocks
    InMemorySagaStateManager<Integer> sagaStateManager;


    @Test
    @DisplayName("SagaPromises - 사가 상태 등록 - 성공")
    void should_initializeState_when_initializingSagaMessageAndPromises() {
        // given
        SagaPromise<?, SagaMessage<Integer>>[] sagaPromises = sagaDefinition.initializeSaga();
        when(sagaStateFactory.create(sagaPromises)).thenReturn(sagaState);

        // when
        sagaStateManager.initializeState(sagaMessage, sagaPromises);

        // then
        verify(sagaStateFactory, only().description("The create() operation should be executed once")).create(sagaPromises);
        verify(sagaState, only().description("The setSuccess() operation should be executed once")).setSuccess(sagaMessage);
    }

    @Test
    @DisplayName("SagaMessage - 사가 상태 업데이트 - 성공")
    void should_updateState_when_updateSagaMessage() {
        // given
        when(sagaMessage.getCorrelationId()).thenReturn(correlationId);
        when(cache.get(correlationId)).thenReturn(sagaState);
        when(sagaState.isDone()).thenReturn(false);

        // when
        sagaStateManager.update(sagaMessage);

        // then
        verify(cache, times(1).description("The cache.get() operation should be executed once")).get(correlationId);
        verify(sagaState, times(1).description("The state.setSuccess() operation should be executed once")).setSuccess(sagaMessage);
        verify(sagaState, times(1).description("The state.isDone() operation should be executed once")).isDone();
    }

    @Test
    @DisplayName("SagaMessage - 사가 상태 업데이트, 사가의 완료 - 캐시에서 제거")
    void should_removeFromCache_when_stateIsDone() {
        // given
        when(sagaMessage.getCorrelationId()).thenReturn(correlationId);
        when(cache.get(correlationId)).thenReturn(sagaState);
        when(sagaState.isDone()).thenReturn(true);

        // when
        sagaStateManager.update(sagaMessage);

        // then
        verify(cache, times(1)
                .description("The cache.get() operation should be executed once")).get(correlationId);
        verify(sagaState, times(1)
                .description("The state.setSuccess() operation should be executed once")).setSuccess(sagaMessage);
        verify(sagaState, times(1)
                .description("The state.isDone() operation should be executed once")).isDone();
        verify(cache, times(1)
                .description("The cache.remove() operation should be executed once")).remove(correlationId);
    }
}