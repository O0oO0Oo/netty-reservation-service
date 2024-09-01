package org.server.rsaga.saga.api.impl;

import io.netty.util.concurrent.Promise;
import lombok.extern.slf4j.Slf4j;
import org.server.rsaga.messaging.consumer.MessageConsumer;
import org.server.rsaga.messaging.message.Message;
import org.server.rsaga.saga.api.SagaCoordinator;
import org.server.rsaga.saga.api.SagaDefinition;
import org.server.rsaga.saga.api.SagaMessage;
import org.server.rsaga.saga.promise.SagaPromise;
import org.server.rsaga.saga.state.SagaStateManager;

@Slf4j
public class DefaultSagaCoordinator<K, V> implements SagaCoordinator<K, V> {
    private final SagaDefinition sagaDefinition;
    private final SagaStateManager<K, V> sagaStateManager;

    public DefaultSagaCoordinator(SagaDefinition sagaDefinition, SagaStateManager<K, V> sagaStateManager, MessageConsumer<K, V> messageConsumer) {
        this.sagaDefinition = sagaDefinition;
        this.sagaStateManager = sagaStateManager;
        messageConsumer.registerHandler(this::handleMessageAndConvert);
    }

    @Override
    public Promise<SagaMessage<K, V>> start(SagaMessage<K, V> initSagaMessage) {
        SagaPromise<?, SagaMessage<K, V>>[] sagaPromises = sagaDefinition.initializeSaga();
        sagaStateManager.initializeState(initSagaMessage, sagaPromises);

        SagaPromise<?, SagaMessage<K, V>> sagaPromise = sagaPromises[sagaPromises.length - 1];
        return sagaPromise.getPromise();
    }

    private void handleMessageAndConvert(Message<K, V> message) {
        if (message instanceof SagaMessage<K, V> sagaMessage) {
            handleMessage(sagaMessage);
        } else {
            // SagaMessage 로 변환
            SagaMessage<K, V> sagaMessage = convertToSagaMessage(message);
            handleMessage(sagaMessage);
        }
    }

    /**
     * 다른 타입의 메시지로 온 객체를 변환한다.
     * @param message
     * @return SagaMessage
     */
    private SagaMessage<K, V> convertToSagaMessage(Message<K, V> message) {
        return SagaMessage.of(message.key(), message.payload(), message.metadata(), message.status());
    }

    @Override
    public void handleMessage(SagaMessage<K, V> sagaMessage) {
        sagaStateManager.update(sagaMessage);
    }
}