package org.server.rsaga.saga.api;

import io.netty.util.concurrent.Promise;

public interface SagaCoordinator<K, V> {
    /**
     * 사가 단계를 시작하고 마지막 단계의 사가 메시지를 리턴한다.
     */
    Promise<SagaMessage<K, V>> start(SagaMessage<K, V> initSagaMessage);

    void handleMessage(SagaMessage<K, V> sagaMessage);
}