package org.server.reservation.saga.step.impl;

import org.server.reservation.saga.step.SagaStep;

/**
 * {@link SagaStep} 의 타입
 */
public enum StepType {
    INITIAL,
    EXECUTE,
    COMPENSATE,
    FINAL
}