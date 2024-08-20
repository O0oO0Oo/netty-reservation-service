package org.server.rsaga.netty.http.mapping;

public interface MethodHandlerFactory<T extends MethodHandler>{
    T createHandler();
}
