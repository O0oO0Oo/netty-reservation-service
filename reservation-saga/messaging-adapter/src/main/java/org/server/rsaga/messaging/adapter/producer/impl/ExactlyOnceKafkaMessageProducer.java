package org.server.rsaga.messaging.adapter.producer.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.server.rsaga.messaging.adapter.producer.KafkaMessageProducer;
import org.server.rsaga.messaging.message.Message;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Properties;

@Slf4j
public class ExactlyOnceKafkaMessageProducer<K, V> implements KafkaMessageProducer<K, V> {
    private final KafkaProducer<K, V> producer;
    private final String transactionId;

    public ExactlyOnceKafkaMessageProducer(Properties properties) {
        this.producer = new KafkaProducer<>(properties);
        this.transactionId = properties.getProperty("transactional.id");
        producer.initTransactions();
    }

    @Override
    public void produce(String destination, Message<K, V> message) {
        try {
            producer.beginTransaction();
            K key = message.key();
            V payload = message.payload();
            Message.Status status = message.status();

            ProducerRecord<K, V> messageRecord = new ProducerRecord<>(destination, key, payload);

            for (Map.Entry<String, byte[]> entry : message.metadata().entrySet()) {
                String headerKey = entry.getKey();
                byte[] headerValue = entry.getValue();
                messageRecord.headers().add(new RecordHeader(headerKey, headerValue));
            }
            messageRecord.headers().add(new RecordHeader(Message.STATUS, status.name().getBytes(StandardCharsets.UTF_8)));

            producer.send(messageRecord);
        } catch (Exception e) {
            log.error("Transaction id {} is aborted, Exception thrown : {}", transactionId, e.getMessage());
            producer.abortTransaction();
        }finally {
            producer.commitTransaction();
            log.debug("Transaction id {} is committed.", transactionId);
        }
    }
}