package org.server.rsaga.saga.state;

import org.server.rsaga.saga.message.SagaMessage;

public interface SagaState<T> {
    void setSuccess(SagaMessage<T> message);
    void setFailure(Throwable cause);
    boolean isDone();
}