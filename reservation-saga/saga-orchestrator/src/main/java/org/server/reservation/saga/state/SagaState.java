package org.server.reservation.saga.state;

import org.server.reservation.saga.message.SagaMessage;

public interface SagaState<T> {
    void setSuccess(SagaMessage<T> message);
    void setFailure(Throwable cause);
    boolean isDone();
}