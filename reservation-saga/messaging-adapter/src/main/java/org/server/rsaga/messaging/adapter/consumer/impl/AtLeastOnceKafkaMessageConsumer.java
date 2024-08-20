package org.server.rsaga.messaging.adapter.consumer.impl;

import com.google.protobuf.DynamicMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.server.rsaga.messaging.adapter.consumer.KafkaMessageConsumer;
import org.server.rsaga.messaging.adapter.message.KafkaMessage;
import org.server.rsaga.messaging.adapter.util.KafkaDynamicMessageConverter;
import org.server.rsaga.messaging.message.Message;

import java.time.Duration;
import java.util.Collections;
import java.util.function.Consumer;

@Slf4j
public class AtLeastOnceKafkaMessageConsumer<K, V> implements KafkaMessageConsumer<K, V> {
    private final KafkaConsumer<K, V> kafkaConsumer;
    private Consumer<Message<K, V>> messageHandler;
    private volatile boolean running = true;

    public AtLeastOnceKafkaMessageConsumer(KafkaConsumer<K, V> kafkaConsumer,
                                           String topic) {
        this.kafkaConsumer = kafkaConsumer;
        kafkaConsumer.subscribe(Collections.singleton(topic));
    }

    @Override
    public void registerHandler(Consumer<Message<K ,V>> messageHandler) {
        this.messageHandler = messageHandler;
        this.start();
    }

    private void start() {
        new Thread(this::pollMessage).start();
    }

    private void pollMessage() {
        try {
            while (running) {
                ConsumerRecords<K, V> messageRecords = kafkaConsumer.poll(Duration.ofMillis(100));
                if (messageHandler != null && !messageRecords.isEmpty()) {
                    for (ConsumerRecord<K, V> messageRecord : messageRecords) {
                        K key = messageRecord.key();
                        V value = messageRecord.value();

                        if(value instanceof DynamicMessage dynamicMessage){
                            value = KafkaDynamicMessageConverter.handleDynamicMessage(dynamicMessage);
                        }

                        Message<K, V> message = KafkaMessage.of(key, value, messageRecord);

                        log.debug("Received message: key={}, payload={}", message.key(), message.payload());
                        messageHandler.accept(message);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Exception occurred. cause : {}", e.getMessage());
            start();
        }
    }
}
