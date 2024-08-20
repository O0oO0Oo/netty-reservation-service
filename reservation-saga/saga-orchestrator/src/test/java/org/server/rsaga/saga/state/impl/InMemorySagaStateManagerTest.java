package org.server.rsaga.saga.state.impl;

import io.hypersistence.tsid.TSID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.server.rsaga.messaging.message.Message;
import org.server.rsaga.saga.api.SagaDefinition;
import org.server.rsaga.saga.api.SagaMessage;
import org.server.rsaga.saga.exception.RemoteServiceException;
import org.server.rsaga.saga.promise.SagaPromise;
import org.server.rsaga.saga.state.SagaState;
import org.server.rsaga.saga.state.SagaStateCache;
import org.server.rsaga.saga.state.factory.SagaStateFactory;

import java.util.UUID;

import static org.mockito.Mockito.*;
/**
 * <pre>
 * 사가의 상태를 관리하는 {@link org.server.rsaga.saga.state.SagaStateManager} 의 테스트
 * </pre>
 */
@DisplayName("SagaStateManager Implementation Tests")
@ExtendWith(MockitoExtension.class)
class InMemorySagaStateManagerTest {

    @Mock
    SagaDefinition sagaDefinition;
    @Mock
    SagaStateFactory<Integer, Integer> sagaStateFactory;
    @Mock
    SagaStateCache<Integer, Integer> cache;
    @Mock
    SagaMessage<Integer, Integer> sagaMessage;
    @Mock
    SagaState<Integer, Integer> sagaState;
    @Mock
    TSID correlationId;

    @InjectMocks
    InMemorySagaStateManager<Integer, Integer> sagaStateManager;


    @Test
    @DisplayName("SagaPromises - 사가 상태 등록 - 성공")
    void should_initializeState_when_initializingSagaMessageAndPromises() {
        // given
        SagaPromise<?, SagaMessage<Integer, Integer>>[] sagaPromises = sagaDefinition.initializeSaga();
        when(sagaStateFactory.create(sagaPromises)).thenReturn(sagaState);

        // when
        sagaStateManager.initializeState(sagaMessage, sagaPromises);

        // then
        verify(sagaStateFactory, only().description("The create() operation should be executed once")).create(sagaPromises);
        verify(sagaState, only().description("The setSuccess() operation should be executed once")).updateState(sagaMessage);
    }

    @Test
    @DisplayName("SagaMessage - 사가 상태 업데이트 - 성공")
    void should_updateState_when_updateSagaMessage() {
        // given
        when(sagaMessage.correlationId()).thenReturn(correlationId);
        when(cache.get(correlationId)).thenReturn(sagaState);
        when(sagaState.isAllDone()).thenReturn(false);

        // when
        sagaStateManager.update(sagaMessage);

        // then
        verify(cache, times(1).description("The cache.get() operation should be executed once")).get(correlationId);
        verify(sagaState, times(1).description("The state.setSuccess() operation should be executed once")).updateState(sagaMessage);
        verify(sagaState, times(1).description("The state.isDone() operation should be executed once")).isAllDone();
    }

    @Test
    @DisplayName("SagaMessage - 사가 상태 업데이트, 사가의 완료 - 캐시에서 제거")
    void should_removeFromCache_when_stateIsDone() {
        // given
        when(sagaMessage.correlationId()).thenReturn(correlationId);
        when(cache.get(correlationId)).thenReturn(sagaState);
        when(sagaState.isAllDone()).thenReturn(true);

        // when
        sagaStateManager.update(sagaMessage);

        // then
        verify(cache, times(1)
                .description("The cache.get() operation should be executed once")).get(correlationId);
        verify(sagaState, times(1)
                .description("The state.setSuccess() operation should be executed once")).updateState(sagaMessage);
        verify(sagaState, times(1)
                .description("The state.isDone() operation should be executed once")).isAllDone();
        verify(cache, times(1)
                .description("The cache.remove() operation should be executed once")).remove(correlationId);
    }
}