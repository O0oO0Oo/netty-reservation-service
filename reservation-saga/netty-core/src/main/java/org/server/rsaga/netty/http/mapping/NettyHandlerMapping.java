package org.server.rsaga.netty.http.mapping;

import io.netty.handler.codec.http.FullHttpRequest;
import org.springframework.context.ApplicationContext;

import java.lang.annotation.Annotation;

public interface NettyHandlerMapping {
    HandlerExecution getHandler(FullHttpRequest request);
    void registerHandlers(ApplicationContext applicationContext, Class<? extends Annotation>[] annotations);
}