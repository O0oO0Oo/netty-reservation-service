package org.server.reservation.saga.state;

import org.server.reservation.saga.message.Key;

public interface SagaStateCache<V> {
    SagaState<V> get(Key key);

    void put(Key key, SagaState<V> value);

    void remove(Key key);
}
