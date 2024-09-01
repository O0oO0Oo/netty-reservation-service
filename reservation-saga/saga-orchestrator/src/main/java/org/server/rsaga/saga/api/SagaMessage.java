package org.server.rsaga.saga.api;

import io.hypersistence.tsid.TSID;
import org.server.rsaga.common.exception.ErrorCode;
import org.server.rsaga.messaging.message.ErrorDetails;
import org.server.rsaga.messaging.message.Message;
import org.server.rsaga.saga.api.impl.DefaultSagaMessage;
import org.server.rsaga.saga.step.impl.StepType;

import java.util.HashMap;
import java.util.Map;

/**
 * @param <K> message 의 키 값
 * @param <V> message payload 값
 */
public interface SagaMessage<K, V> extends Message<K, V>, ErrorDetails {
    /**
     * @return Returns a TSID-based key.
     */
    TSID correlationId();
    int stepId();
    StepType stepType();

    String CORRELATION_ID = "CORRELATION_ID";
    String STEP_TYPE = "STEP_TYPE";
    String STEP_ID = "STEP_ID";

    static <K, V> SagaMessage<K, V> of(K key, V payload, Map<String, byte[]> metadata, Status status) {
        return new DefaultSagaMessage<>(key, payload, metadata, status);
    }

    static <K, V> SagaMessage<K, V> of(K key, V payload, Status status) {
        return new DefaultSagaMessage<>(key, payload, new HashMap<>(), status);
    }

    static <K, V> SagaMessage<K, V> createFailureResponse(Message<K, V> message, ErrorCode errorCode) {
        Map<String, byte[]> metadata = message.metadata();

        metadata.put(ErrorDetails.ERROR_CODE, errorCode.getCode().getBytes());
        metadata.put(ErrorDetails.ERROR_MESSAGE, errorCode.getMessage().getBytes());

        return new DefaultSagaMessage<>(message.key(), message.payload(), message.metadata(), Status.RESPONSE_FAILURE);
    }
}