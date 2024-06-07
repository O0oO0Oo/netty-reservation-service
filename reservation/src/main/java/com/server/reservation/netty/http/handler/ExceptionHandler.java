package com.server.reservation.netty.http.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

// TODO : Exception Handler 구현, ControllerAdvice
@Slf4j
public class ExceptionHandler extends ChannelHandlerAdapter {
    
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        String message = cause.getMessage();
        if (Objects.isNull(message)) {
            message = "Message is null.";
        }

        log.error(cause + " : " + message);

        ByteBuf responseContent = Unpooled.copiedBuffer(message.getBytes());
        DefaultFullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1, HttpResponseStatus.OK, responseContent
        );
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, String.valueOf(responseContent.readableBytes()));

        ctx.writeAndFlush(response);
    }
}
