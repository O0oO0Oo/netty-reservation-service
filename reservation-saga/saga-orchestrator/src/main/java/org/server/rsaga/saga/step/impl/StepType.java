package org.server.rsaga.saga.step.impl;

/**
 * {@link org.server.rsaga.saga.step.SagaStep} 의 타입
 */
public enum StepType {
    INITIAL,
    EXECUTE,
    COMPENSATE,
    FINAL
}