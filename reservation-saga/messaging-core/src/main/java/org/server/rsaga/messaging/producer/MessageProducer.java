package org.server.rsaga.messaging.producer;

import org.server.rsaga.messaging.message.Message;

public interface MessageProducer<K, V> {
    void produce(String destination, Message<K, V> message);
}