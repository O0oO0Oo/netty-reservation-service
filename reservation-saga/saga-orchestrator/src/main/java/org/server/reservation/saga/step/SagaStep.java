package org.server.reservation.saga.step;

import org.server.reservation.saga.step.impl.StepType;

/**
 * <pre>
 * SagaStep 은 Orchestration Saga 전체 과정을 이루는 요소로 각 단계에서 어떤 내용의 이벤트를 발행할지 결정한다.
 * 이전 단계의 결과를 하나 또는 여러개를 모아 전처리하여 이벤트를 발행한다.
 * </pre>
 */
public interface SagaStep {
    StepType getStepType();
}