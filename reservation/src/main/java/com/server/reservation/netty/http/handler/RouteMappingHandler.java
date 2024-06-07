package com.server.reservation.netty.http.handler;

import com.server.reservation.common.config.SpringApplicationContext;
import com.server.reservation.common.response.FullHttpResponseBuilder;
import com.server.reservation.netty.http.mapping.HandlerExecution;
import com.server.reservation.netty.http.mapping.NettyHandlerMapping;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import lombok.extern.slf4j.Slf4j;

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
        Object retValue = methodHandler.execute();
        FullHttpResponse response;

        if(retValue instanceof FullHttpResponse fullHttpResponse) {
            response = fullHttpResponse;
        }
        else {
            response = FullHttpResponseBuilder.builder()
                    .body(retValue)
                    .build();
        }

        ctx.writeAndFlush(response);
    }
}