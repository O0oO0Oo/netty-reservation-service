package org.server.rsaga.saga.state.impl;

import io.hypersistence.tsid.TSID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.server.rsaga.saga.state.SagaState;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * <pre>
 * InMemory 로 상태를 관리할 때 캐시의 동작 테스트
 *
 * ## Concurrency
 * key 는 항상 유니크하게 만들어진다, 또한 상태 변경은 Value 내부에서 이루어지기 때문에 테스트 할 필요가 없다.
 * </pre>
 */
@DisplayName("SagaStateCache Implementation Tests")
@ExtendWith(MockitoExtension.class)
class InMemoryGuavaSagaStateCacheTest {
    @Mock
    TSID key;
    @Mock
    SagaState<Object, Object> expectedSagaState;

    InMemoryGuavaSagaStateCache<Object, Object> cache;

    @BeforeEach
    void beforeEach() {
        cache = new InMemoryGuavaSagaStateCache<>(20000, 5);
    }

    @Test
    @DisplayName("SagaState, Cache - get 동작 - 성공")
    void should_returnValue_when_keyExists() {
        // given

        // when
        cache.put(key, expectedSagaState);
        SagaState<Object, Object> actualSagaState = cache.get(key);

        // then
        assertEquals(expectedSagaState, actualSagaState, "The data in put and the result in get should be the same.");
    }

    @Test
    @DisplayName("SagaState, Cache - remove 동작 - 성공")
    void should_cacheValue_when_putCalled() {
        // given

        // when
        cache.put(key, expectedSagaState);
        SagaState<Object, Object> actualSagaState = cache.get(key);
        cache.remove(key);
        SagaState<Object, Object> removedResult = cache.get(key);

        // then
        assertEquals(expectedSagaState, actualSagaState, "The data in put and the result in get should be the same.");
        assertNull(removedResult, "Since the data has been removed, the data should not exist.");
    }
}