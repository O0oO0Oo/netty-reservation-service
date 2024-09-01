package org.server.rsaga.common.netty;

import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.DefaultExecutorServiceFactory;
import io.netty.util.concurrent.EventExecutorGroup;
import io.netty.util.concurrent.Promise;
import org.springframework.stereotype.Component;

/**
 * 비동기 응답에서 반환 값 변환을 위한 스레드 풀
 */
@Component
public class SharedResponseEventExecutorGroup {
    private final EventExecutorGroup eventExecutorGroup = new DefaultEventExecutorGroup(
            8,
            new DefaultExecutorServiceFactory("async-response-conversion-thread-group")); // 스레드 풀 크기 설정

    public EventExecutorGroup getEventExecutorGroup() {
        return eventExecutorGroup;
    }

    public <V> Promise<V> getPromise() {
        return eventExecutorGroup.next().newPromise();
    }
}