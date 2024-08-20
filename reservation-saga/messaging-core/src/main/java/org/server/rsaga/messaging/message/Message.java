package org.server.rsaga.messaging.message;

import java.util.Map;

public interface Message<K, V> {
    String STATUS = "STATUS";

    enum Status{
        REQUEST,
        RESPONSE_SUCCESS,
        RESPONSE_FAILED,
        IN_PROGRESS;
    }
    K key();
    V payload();

    Map<String, byte[]> metadata();
    Status status();
}