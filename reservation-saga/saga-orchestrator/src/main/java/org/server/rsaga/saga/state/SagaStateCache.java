package org.server.rsaga.saga.state;

import org.server.rsaga.saga.message.Key;

public interface SagaStateCache<V> {
    SagaState<V> get(Key key);

    void put(Key key, SagaState<V> value);

    void remove(Key key);
}
