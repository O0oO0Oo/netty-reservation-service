package org.server.rsaga.messaging.adapter.execption;

public class KafkaPartitionException extends RuntimeException {
    public KafkaPartitionException(String message) {
        super(message);
    }

    public KafkaPartitionException(String message, Throwable cause) {
        super(message, cause);
    }
}
