package org.server.reservation.netty.http.handler;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import lombok.extern.slf4j.Slf4j;
import org.server.reservation.config.SpringApplicationContext;
import org.server.reservation.core.common.dto.FullHttpResponseBuilder;
import org.server.reservation.netty.http.mapping.HandlerExecution;
import org.server.reservation.netty.http.mapping.NettyHandlerMapping;

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