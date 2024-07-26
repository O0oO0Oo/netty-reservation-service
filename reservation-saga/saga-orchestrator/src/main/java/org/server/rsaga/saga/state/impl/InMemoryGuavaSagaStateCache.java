package org.server.rsaga.saga.state.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalCause;
import lombok.extern.slf4j.Slf4j;
import org.server.rsaga.saga.message.Key;
import org.server.rsaga.saga.state.SagaState;
import org.server.rsaga.saga.state.SagaStateCache;

import javax.naming.TimeLimitExceededException;
import java.util.concurrent.TimeUnit;

@Slf4j
public class InMemoryGuavaSagaStateCache<V> implements SagaStateCache<V> {
    private int maximumCacheSize = 20000;
    private int expireDuration = 5;

    public InMemoryGuavaSagaStateCache(int maximumCacheSize, int expireDuration) {
        this.maximumCacheSize = maximumCacheSize;
        this.expireDuration = expireDuration;
    }

    private final Cache<Key, SagaState<V>> cache = CacheBuilder.newBuilder()
            .maximumSize(maximumCacheSize)
            .expireAfterAccess(expireDuration, TimeUnit.SECONDS)
            .concurrencyLevel(1)
            .recordStats()
            .removalListener(
                    notification -> handleExpire(
                            (Key) notification.getKey(),
                            (SagaState<V>) notification.getValue(),
                            notification.getCause()
                    )
            )
            .build();

    @Override
    public SagaState<V> get(Key key) {
        return cache.getIfPresent(key);
    }

    @Override
    public void put(Key key, SagaState<V> value) {
        cache.put(key, value);
    }

    @Override
    public void remove(Key key) {
        cache.invalidate(key);
    }

    private void handleExpire(Key key, SagaState<V> value, RemovalCause cause) {
        if(cause.equals(RemovalCause.EXPIRED)){
            log.error("SagaState with correlation id {}, status {} was evict because it expired.", key, value);
            if (!value.isDone()) {
                value.setFailure(new TimeLimitExceededException("Request timeout."));
                cache.put(key, value);
            }
        }
    }
}