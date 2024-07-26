package org.server.rsaga.saga.message;

public interface MessageProducer<T> {
    void produce(String destination, SagaMessage<T> sagaMessage);
}