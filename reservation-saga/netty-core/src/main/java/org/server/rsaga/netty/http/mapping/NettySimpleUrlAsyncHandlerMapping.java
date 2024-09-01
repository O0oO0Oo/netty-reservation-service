package org.server.rsaga.netty.http.mapping;

import org.server.rsaga.common.annotation.AsyncResponse;
import org.server.rsaga.common.dto.FullHttpAsyncResponse;

import java.lang.reflect.Method;

public class NettySimpleUrlAsyncHandlerMapping<T extends MethodHandler> extends AbstractNettyUrlHandlerMapping<T> {

    public NettySimpleUrlAsyncHandlerMapping(MethodHandlerFactory<T> handlerFactor) {
        super(handlerFactor);
    }

    @Override
    protected boolean shouldRegisterMethod(Method method) {
        // AsyncResponse 어노테이션이 붙어있고, 반환 타입이 FullHttpAsyncResponse 인 메서드만 등록
        if (!method.isAnnotationPresent(AsyncResponse.class)) {
            return false;
        }

        // AsyncResponse 어노테이션이 붙은 메서드는 반환타입이 FullHttpAsyncResponse 여야 한다.
        if (!FullHttpAsyncResponse.class.isAssignableFrom(method.getReturnType())) {
            throw new IllegalStateException("AsyncResponse annotated methods must return FullHttpAsyncResponse");
        }

        return true;
    }
}