package org.server.rsaga.netty.http.handler;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.util.concurrent.EventExecutorGroup;
import lombok.extern.slf4j.Slf4j;
import org.server.rsaga.common.dto.FullHttpResponseBuilder;
import org.server.rsaga.netty.http.mapping.HandlerExecution;
import org.server.rsaga.netty.http.mapping.NettyHandlerMapping;

import java.lang.reflect.InvocationTargetException;

/**
 * 동기적 응답을 위한 핸들러.
 */
@Slf4j
@ChannelHandler.Sharable
public class RouteMappingHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    private final NettyHandlerMapping nettyHandlerMapping;
    private final EventExecutorGroup executorGroup;

    public RouteMappingHandler(NettyHandlerMapping nettyHandlerMapping, EventExecutorGroup executorGroup) {
        this.nettyHandlerMapping = nettyHandlerMapping;
        this.executorGroup = executorGroup;
    }

    @Override
    protected void messageReceived(ChannelHandlerContext ctx, FullHttpRequest msg) {
        msg.retain(); // 비동기 작업에 들어가면서 참조 카운트가 감소하게 된다.
        executorGroup.submit(() -> handleRequest(ctx, msg));
    }

    private void handleRequest(ChannelHandlerContext ctx, FullHttpRequest msg) {
        try {
            HandlerExecution methodHandler = nettyHandlerMapping.getHandler(msg);
            if (methodHandler != null) {
                FullHttpResponse response = createResponse(methodHandler);
                ctx.writeAndFlush(response);
            } else {
                // 매칭되는 컨트롤러 동기적 메서드가 없다면 비동기적 메서드를 찾는다.
                ctx.fireChannelRead(msg.retain());
            }
        } catch (Exception e) {
            ctx.fireExceptionCaught(e);
        } finally {
            msg.release();
        }
    }

    private FullHttpResponse createResponse(HandlerExecution methodHandler) throws InvocationTargetException, IllegalAccessException {
        Object retValue = methodHandler.execute();

        if (retValue instanceof FullHttpResponse fullHttpResponse) {
            return fullHttpResponse;
        } else {
            return FullHttpResponseBuilder.builder()
                    .body(retValue)
                    .statusCode(HttpResponseStatus.OK)
                    .build();
        }
    }
}