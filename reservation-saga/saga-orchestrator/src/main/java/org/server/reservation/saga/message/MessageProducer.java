package org.server.reservation.saga.message;

public interface MessageProducer<T> {
    void produce(String destination, SagaMessage<T> sagaMessage);
}