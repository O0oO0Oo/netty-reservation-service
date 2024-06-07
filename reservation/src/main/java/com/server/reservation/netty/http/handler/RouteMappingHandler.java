package com.server.reservation.netty.http.handler;

import com.server.reservation.common.config.SpringApplicationContext;
import com.server.reservation.netty.http.mapping.HandlerExecution;
import com.server.reservation.netty.http.mapping.NettyHandlerMapping;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;

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
        Object body = methodHandler.execute();

        String responseBody = body.toString();
        ByteBuf responseContent = Unpooled.copiedBuffer(responseBody, StandardCharsets.UTF_8);
        DefaultFullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1, HttpResponseStatus.OK, responseContent
        );
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, String.valueOf(responseContent.readableBytes()));

        ctx.writeAndFlush(response);
    }
}