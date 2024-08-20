package org.server.rsaga.saga.state;

import io.hypersistence.tsid.TSID;

public interface SagaStateCache<K, V> {
    SagaState<K, V> get(TSID key);

    void put(TSID key, SagaState<K, V> value);

    void remove(TSID key);
}