package org.server.rsaga.messaging.adapter.producer.util;

import lombok.extern.slf4j.Slf4j;
import org.server.rsaga.messaging.adapter.message.KafkaMessage;
import org.server.rsaga.messaging.message.ErrorDetails;
import org.server.rsaga.messaging.message.Message;
import org.server.rsaga.messaging.producer.MessageProducer;
import org.server.rsaga.messaging.util.DeadLetterHandler;

import java.util.List;
import java.util.Map;

@Slf4j
public class KafkaDeadLetterHandler<K, V> implements DeadLetterHandler<K, V> {
    private final MessageProducer<K, V> messageProducer;
    private final String dlqTopic;

    public KafkaDeadLetterHandler(MessageProducer<K, V> messageProducer, String dlqTopic) {
        this.messageProducer = messageProducer;
        this.dlqTopic = dlqTopic;
    }

    @Override
    public void handle(Message<K, V> message, Throwable e) {
        Map<String, byte[]> metadata = message.metadata();

        metadata.put(ErrorDetails.ERROR_CODE, "B000".getBytes());
        metadata.put(ErrorDetails.ERROR_MESSAGE, e.getMessage().getBytes());
        metadata.put(Message.STATUS, Message.Status.RESPONSE_FAILED.name().getBytes());

        KafkaMessage<K, V> errorMessage = KafkaMessage.of(message.key(), message.payload(), metadata, Message.Status.RESPONSE_FAILED);
        messageProducer.produce(dlqTopic, errorMessage);
    }

    @Override
    public void handle(List<Message<K, V>> messages, Throwable e) {
        for (Message<K, V> message : messages) {
            Map<String, byte[]> metadata = message.metadata();

            metadata.put(ErrorDetails.ERROR_CODE, "B000".getBytes());
            metadata.put(ErrorDetails.ERROR_MESSAGE, e.getMessage().getBytes());
            metadata.put(Message.STATUS, Message.Status.RESPONSE_FAILED.name().getBytes());

            KafkaMessage<K, V> errorMessage = KafkaMessage.of(message.key(), message.payload(), metadata, Message.Status.RESPONSE_FAILED);
            messageProducer.produce(dlqTopic, errorMessage);
        }
    }
}