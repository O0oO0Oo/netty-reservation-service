package org.server.rsaga.netty.http.mapping;

import org.server.rsaga.common.annotation.AsyncResponse;

import java.lang.reflect.Method;

public class NettySimpleUrlHandlerMapping<T extends MethodHandler> extends AbstractNettyUrlHandlerMapping<T> {

    public NettySimpleUrlHandlerMapping(MethodHandlerFactory<T> handlerFactor) {
        super(handlerFactor);
    }

    @Override
    protected boolean shouldRegisterMethod(Method method) {
        // AsyncResponse 어노테이션이 없는 메서드만 등록
        return !method.isAnnotationPresent(AsyncResponse.class);
    }
}
