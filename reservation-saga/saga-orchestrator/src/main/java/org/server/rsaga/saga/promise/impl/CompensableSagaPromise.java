package org.server.rsaga.saga.promise.impl;

import com.google.common.base.Preconditions;
import io.netty.util.concurrent.Promise;
import lombok.extern.slf4j.Slf4j;
import org.server.rsaga.saga.api.SagaMessage;
import org.server.rsaga.saga.promise.AbstractSagaPromise;
import org.server.rsaga.saga.step.impl.StepType;

import java.util.function.Consumer;

/**
 * <pre>
 * 기존 Promise 의 경우 setSuccess 를 할경우 fail 로 설정하지 못하며,
 * 그에 따른 listener 를 호출하지 못한다.
 * 따라서 상태 설정 이후에도 추가적인 동작을 할 수 있도록 구성하였다.
 * </pre>
 */
@Slf4j
public final class CompensableSagaPromise<I, R extends SagaMessage<?, ?>> extends AbstractSagaPromise<I, R> {
    private final Consumer<I> compensate;
    private boolean needCompensation = false;
    private boolean isCompensationPerformed = false;
    private boolean isCompensationComplete = false;
    private I input;

    public CompensableSagaPromise(Promise<R> executeNextPromise, Consumer<I> execute, Consumer<I> compensate) {
        super(executeNextPromise, execute);
        this.compensate = compensate;
    }

    @Override
    public void execute(I input) {
        this.input = input;
        super.execute(input);
    }

    @Override
    public void success(R result) {
        Preconditions.checkNotNull(result, "The result value should not be null.");
        StepType stepType = result.stepType();

        // 요청 - "현 성공" - 끝
        if (!executePromise.isDone() && isExecutionPerformed() && stepType.equals(StepType.EXECUTE)) {
            // 요청 - 다른 작업 실패 - "현 성공" - 보상 - 보상 성공 - 끝
            if (needCompensation) {
                needCompensation = false;
                isCompensationPerformed = true;
                compensate.accept(input);
            }

            executePromise.setSuccess(result);
        }

        // 요청 - ... - 보상 - "보상 성공" - 끝
        if (stepType.equals(StepType.COMPENSATE)) {
            isCompensationComplete = true;
        }
    }

    /**
     * 현재 단계가 실패했다면 compensation 요청이 나가지 않는다.
     * @param cause
     */
    @Override
    public void failure(R result, Throwable cause) {
        StepType stepType = result.stepType();

        // 실패한게 execute 일 경우 나가지 말아야함.
        if (!executePromise.isDone() && stepType.equals(StepType.EXECUTE)) {
            needCompensation = false;
            executePromise.setFailure(cause);
        }

        // 실패한게 compensate 일 경우 다시 나가야함? 아니면 로깅으로 처리?
        if (stepType.equals(StepType.COMPENSATE)) {
            log.error("The compensation transaction failed. id : {}", result.correlationId());
        }
    }

    /**
     * 다른 단계로 인해 실패한다면 등록된 동작 실행 유무에 따라 compensation 요청이 나간다.
     */
    @Override
    public void cancelDueToOtherFailure(Throwable cause) {

        // 요청 - 성공 - "다른 작업 실패" - "보상" - 보상 성공 - 끝
        if (isExecutionPerformed() && executePromise.isSuccess() && !isCompensationPerformed) {
            isCompensationPerformed = true;
            compensate.accept(input);
        }

        // 요청 - "다른 작업 실패" - "대기" -  현 성공 - 보상 - 보상 성공 - 끝
        if (isExecutionPerformed() && !executePromise.isDone()) {
            needCompensation = true;
        }

        // 요청 전 - "끝"
        if (!isExecutionPerformed() && !executePromise.isDone()) {
            executePromise.setFailure(cause);
        }
    }

    @Override
    public boolean isDone() {
        return super.isDone() && hasCompensated();
    }

    private boolean hasCompensated() {
        if (needCompensation) {
            return false;
        }

        if(isCompensationPerformed){
            return isCompensationComplete;
        }
        else{
            return true;
        }
    }
}