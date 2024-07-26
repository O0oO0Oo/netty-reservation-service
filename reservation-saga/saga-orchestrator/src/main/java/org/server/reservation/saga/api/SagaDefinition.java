package org.server.reservation.saga.api;


import org.server.reservation.saga.message.SagaMessage;
import org.server.reservation.saga.api.factory.SagaDefinitionFactoryImpl;
import org.server.reservation.saga.promise.SagaPromise;

/**
 * <pre>
 * {@link SagaDefinitionFactoryImpl}
 * 으로 설정한 내용을 바탕으로 각 단계들을 관리할 {@link SagaPromise} 체인 리스트 를 만든다.
 * </pre>
 */
public interface SagaDefinition<T> {
    SagaPromise<?, SagaMessage<T>>[] initializeSaga();
}