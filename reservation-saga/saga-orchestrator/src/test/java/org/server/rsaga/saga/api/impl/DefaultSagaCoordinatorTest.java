package org.server.rsaga.saga.api.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.server.rsaga.saga.api.SagaDefinition;
import org.server.rsaga.saga.message.MessageConsumer;
import org.server.rsaga.saga.message.SagaMessage;
import org.server.rsaga.saga.promise.SagaPromise;
import org.server.rsaga.saga.state.SagaStateManager;

import java.util.function.Consumer;

import static org.mockito.Mockito.*;

/**
 * 사가 작업의 시작, 응답을 받아 처리하는 {@link org.server.rsaga.saga.api.SagaCoordinator} 의 테스트
 */
@ExtendWith(MockitoExtension.class)
class DefaultSagaCoordinatorTest {
    @Mock
    SagaDefinition<Integer> sagaDefinition;
    @Mock
    SagaStateManager<Integer> sagaStateManager;
    @Mock
    MessageConsumer<Integer> messageConsumer;

    @InjectMocks
    DefaultSagaCoordinator<Integer> sagaCoordinator;

    @Captor
    private ArgumentCaptor<Consumer<SagaMessage<Integer>>> messageCaptor;

    @Test
    @DisplayName("SagaMessage - 사가 요청 시작 - 성공")
    public void should_success_when_startSaga() {
        // given
        SagaMessage<Integer> initSagaMessage = mock(SagaMessage.class);
        SagaPromise<?, SagaMessage<Integer>>[] sagaPromises = new SagaPromise[]{};
        when(sagaDefinition.initializeSaga()).thenReturn(sagaPromises);

        // when
        sagaCoordinator.start(initSagaMessage);

        // then
        verify(sagaDefinition, only().description("The SagaPromise should be created only once."))
                .initializeSaga();
        verify(sagaStateManager, only().description("The initialization of SagaState should be performed once."))
                .initializeState(initSagaMessage, sagaPromises);
    }

    @Test
    @DisplayName("SagaMessage - 상태 업데이트 - 성공")
    public void testHandleMessage() {
        // given
        SagaMessage<Integer> sagaMessage = mock(SagaMessage.class);

        // when
        sagaCoordinator.handleMessage(sagaMessage);
        sagaCoordinator.handleMessage(sagaMessage);

        // then
        verify(sagaStateManager, times(2).description("The update method should be called twice."))
                .update(sagaMessage);
    }

    @Test
    @DisplayName("MessageConsumer 의 등록 - 메시지 소비 - 성공")
    public void testMessageHandlerRegistration() {
        verify(messageConsumer, only().description("The MessageConsumer should be registered only once"))
                .registerHandler(messageCaptor.capture());

        // given
        SagaMessage<Integer> sagaMessage = mock(SagaMessage.class);

        // when
        messageCaptor.getValue().accept(sagaMessage);

        // Verify handleMessage is called
        verify(sagaStateManager, only().description("The update method should be called once.")).update(sagaMessage);
    }
}