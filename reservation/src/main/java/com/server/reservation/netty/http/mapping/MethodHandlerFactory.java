package com.server.reservation.netty.http.mapping;

public interface MethodHandlerFactory<T extends MethodHandler>{
    T createHandler();
}
