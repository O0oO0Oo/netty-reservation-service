package org.server.rsaga.messaging.adapter.producer.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.server.rsaga.messaging.adapter.producer.KafkaMessageProducer;
import org.server.rsaga.messaging.message.Message;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Properties;

@Slf4j
public class AtLeastOnceKafkaMessageProducer<K, V> implements KafkaMessageProducer<K, V> {
    private final KafkaProducer<K, V> producer;

    public AtLeastOnceKafkaMessageProducer(Properties properties) {
        this.producer = new KafkaProducer<>(properties);
    }

    @Override
    public void produce(String destination, Message<K, V> message) {
        try {
            K key = message.key();
            V payload = message.payload();
            Message.Status status = message.status();

            ProducerRecord<K, V> messageRecord = new ProducerRecord<>(destination, key, payload);

            messageRecord.headers().add(new RecordHeader(Message.STATUS, status.name().getBytes(StandardCharsets.UTF_8)));
            for (Map.Entry<String, byte[]> entry : message.metadata().entrySet()) {
                String headerKey = entry.getKey();
                byte[] headerValue = entry.getValue();
                messageRecord.headers().add(new RecordHeader(headerKey, headerValue));
            }

            producer.send(messageRecord);
        } catch (Exception e) {
            log.error("Exception thrown : {}", e.getMessage());
        }
    }
}