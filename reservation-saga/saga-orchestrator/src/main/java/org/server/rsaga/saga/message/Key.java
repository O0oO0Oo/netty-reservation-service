package org.server.rsaga.saga.message;

/**
 * Key 는 다음 두 가지를 필수적으로 구현해야 하며 유일성을 보장해야 한다.
 */
public interface Key {
    @Override
    boolean equals(Object obj);

    @Override
    int hashCode();
}