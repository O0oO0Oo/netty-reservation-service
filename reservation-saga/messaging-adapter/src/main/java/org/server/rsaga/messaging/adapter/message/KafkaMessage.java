package org.server.rsaga.messaging.adapter.message;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.server.rsaga.messaging.message.Message;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public record KafkaMessage<K, V> (
    K key,
    V payload,
    Map<String, byte[]> metadata,
    Message.Status status
) implements Message<K, V> {

    public static <K, V> KafkaMessage<K, V> of(K key, V payload, ConsumerRecord<K, V> messageRecord) {
        HashMap<String, byte[]> metadata = new HashMap<>();
        for (Header header : messageRecord.headers()) {
            metadata.put(header.key(), header.value());
        }

        byte[] statusBytes = metadata.remove(Message.STATUS);
        String name = new String(statusBytes, StandardCharsets.UTF_8);
        Status status = Status.valueOf(name);

        return new KafkaMessage<>(key, payload, metadata, status);
    }

    public static <K, V> KafkaMessage<K, V> of(K key, V payload, Map<String, byte[]> metadata, Status status) {
        return new KafkaMessage<>(key, payload, metadata, status);
    }
}
