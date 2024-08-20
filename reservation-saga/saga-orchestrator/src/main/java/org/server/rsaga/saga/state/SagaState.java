package org.server.rsaga.saga.state;

import org.server.rsaga.saga.api.SagaMessage;

public interface SagaState<K, V> {
    void updateState(SagaMessage<K, V> message);
    void handleException(Throwable cause);
    boolean isAllDone();
}