package org.server.rsaga.saga.api.impl;

import io.hypersistence.tsid.TSID;
import org.server.rsaga.saga.api.SagaMessage;
import org.server.rsaga.saga.step.SagaStep;
import org.server.rsaga.saga.step.impl.StepType;
import org.server.rsaga.saga.util.SagaMessageUtil;

import java.util.Map;

public record DefaultSagaMessage<K, V>(K key,
                                       V payload,
                                       Map<String, byte[]> metadata,
                                       Status status
) implements SagaMessage<K, V> {

    @Override
    public TSID correlationId() {
        return SagaMessageUtil.extractTsid(
                metadata.get(CORRELATION_ID)
        );
    }

    @Override
    public int stepId() {
        return SagaMessageUtil.extractInt(
                metadata.get(STEP_ID)
        );
    }

    @Override
    public StepType stepType() {
        return StepType.fromBytes(
                metadata.get(SagaMessage.STEP_TYPE)
        );
    }

    @Override
    public Status status() {
        return status;
    }

    @Override
    public String errorCode() {
        return SagaMessageUtil.extractString(metadata.get(ERROR_CODE));
    }

    @Override
    public String errorMessage() {
        return SagaMessageUtil.extractString(metadata.get(ERROR_MESSAGE));
    }
}