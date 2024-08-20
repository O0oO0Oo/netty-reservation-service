package org.server.rsaga.netty.http.mapping;

import io.netty.handler.codec.http.FullHttpRequest;

public interface NettyHandlerMapping {
    HandlerExecution getHandler(FullHttpRequest request) throws Exception;
}