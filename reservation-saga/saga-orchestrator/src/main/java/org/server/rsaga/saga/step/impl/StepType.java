package org.server.rsaga.saga.step.impl;

/**
 * {@link org.server.rsaga.saga.step.SagaStep} 의 타입
 */
public enum StepType {
    INITIAL,
    EXECUTE,
    COMPENSATE,
    ;

    public static StepType fromBytes(byte[] bytes) {
        String value = new String(bytes);
        return StepType.valueOf(value);
    }
}