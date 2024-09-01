package org.server.rsaga.saga.state.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalCause;
import io.hypersistence.tsid.TSID;
import lombok.extern.slf4j.Slf4j;
import org.server.rsaga.saga.state.SagaState;
import org.server.rsaga.saga.state.SagaStateCache;

import javax.naming.TimeLimitExceededException;
import java.util.concurrent.TimeUnit;

@Slf4j
public class InMemoryGuavaSagaStateCache<K, V> implements SagaStateCache<K, V> {
    private int maximumCacheSize = 100000;
    private int expireDuration = 120;

    public InMemoryGuavaSagaStateCache(int maximumCacheSize, int expireDuration) {
        this.maximumCacheSize = maximumCacheSize;
        this.expireDuration = expireDuration;
    }

    private final Cache<TSID, SagaState<K, V>> cache = CacheBuilder.newBuilder()
            .maximumSize(maximumCacheSize)
            .expireAfterAccess(expireDuration, TimeUnit.SECONDS)
            .concurrencyLevel(1)
            .recordStats()
            .removalListener(
                    notification -> handleExpire(
                            (TSID) notification.getKey(),
                            (SagaState<K, V>) notification.getValue(),
                            notification.getCause()
                    )
            )
            .build();

    @Override
    public SagaState<K, V> get(TSID key) {
        return cache.getIfPresent(key);
    }

    @Override
    public void put(TSID key, SagaState<K, V> value) {
        cache.put(key, value);
    }

    @Override
    public void remove(TSID key) {
        cache.invalidate(key);
    }

    private void handleExpire(TSID key, SagaState<K, V> value, RemovalCause cause) {
        if(cause.equals(RemovalCause.EXPIRED)){
            log.info("SagaState with correlation id {}, status {} was evict because it expired.", key, value);
            if (!value.isAllDone()) {
                value.handleException(new TimeLimitExceededException("Request timeout."));
                cache.put(key, value);
            }
        }
    }
}