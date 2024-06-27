package org.server.reservation.netty.http.mapping;

public class HttpMethodHandlerFactory implements MethodHandlerFactory<HttpMethodHandler> {
    @Override
    public HttpMethodHandler createHandler() {
        return new HttpMethodHandler();
    }
}