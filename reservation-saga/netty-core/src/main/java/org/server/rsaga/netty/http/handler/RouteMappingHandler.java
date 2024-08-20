package org.server.rsaga.netty.http.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import lombok.extern.slf4j.Slf4j;
import org.server.rsaga.common.dto.FullHttpResponseBuilder;
import org.server.rsaga.netty.config.SpringApplicationContext;
import org.server.rsaga.netty.http.mapping.HandlerExecution;
import org.server.rsaga.netty.http.mapping.NettyHandlerMapping;

import java.lang.reflect.InvocationTargetException;

@Slf4j
@ChannelHandler.Sharable
public class RouteMappingHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private final NettyHandlerMapping nettyHandlerMapping;

    public RouteMappingHandler() {
        this.nettyHandlerMapping = SpringApplicationContext.getBean(NettyHandlerMapping.class);
    }

    @Override
    protected void messageReceived(ChannelHandlerContext ctx, FullHttpRequest msg) throws Exception {
        HandlerExecution methodHandler = nettyHandlerMapping.getHandler(msg);

        FullHttpResponse response = createResponse(methodHandler);

        ctx.writeAndFlush(response);
    }

    private FullHttpResponse createResponse(HandlerExecution methodHandler) throws JsonProcessingException, InvocationTargetException, IllegalAccessException {
        if (methodHandler == null) {
            return FullHttpResponseBuilder.builder()
                    .body("The requested resource was not found.")
                    .statusCode(HttpResponseStatus.NOT_FOUND)
                    .build();
        }
        else {
            Object retValue = methodHandler.execute();

            if (retValue instanceof FullHttpResponse) {
                return (FullHttpResponse) retValue;
            } else {
                return FullHttpResponseBuilder.builder()
                        .body(retValue)
                        .statusCode(HttpResponseStatus.OK)
                        .build();
            }
        }
    }
}