package org.server.rsaga.netty.http.handler;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.util.concurrent.EventExecutorGroup;
import lombok.extern.slf4j.Slf4j;
import org.server.rsaga.common.dto.FullHttpAsyncResponse;
import org.server.rsaga.common.dto.FullHttpResponseBuilder;
import org.server.rsaga.netty.http.mapping.HandlerExecution;
import org.server.rsaga.netty.http.mapping.NettyHandlerMapping;

import java.lang.reflect.InvocationTargetException;

/**
 * 논 블로킹, 비동기적 응답을 위한 핸들러. {@link org.server.rsaga.common.annotation.AsyncResponse} 가 붙은 컨트롤러 메서드를 실행하게 된다.
 */
@Slf4j
@ChannelHandler.Sharable // 여러 채널 공유
public class AsyncRouteMappingHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    private final NettyHandlerMapping nettyHandlerMapping;
    private final EventExecutorGroup executorGroup;

    public AsyncRouteMappingHandler(NettyHandlerMapping nettyHandlerMapping, EventExecutorGroup executorGroup) {
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
            createAsyncResponse(methodHandler, ctx);
        } catch (Exception e) {
            ctx.fireExceptionCaught(e);
        } finally {
            msg.release();
        }
    }

    private void createAsyncResponse(HandlerExecution methodHandler, ChannelHandlerContext ctx) throws InvocationTargetException, IllegalAccessException {
        if (methodHandler == null) {
            // 매칭되는 url 이 없다면 404 응답.
            sendNotFoundResponse(ctx);
        }
        else {
            // 있다면 메서드 실행
            processAsyncResponse(methodHandler, ctx);
        }
    }

    private void processAsyncResponse(HandlerExecution methodHandler, ChannelHandlerContext ctx) throws InvocationTargetException, IllegalAccessException {
        if (methodHandler == null) {
            sendNotFoundResponse(ctx);
        } else {
            Object result = methodHandler.execute();
            if (result instanceof FullHttpAsyncResponse fullHttpAsyncResponse) {
                fullHttpAsyncResponse.processResult(ctx);
            } else {
                sendErrorResponse(ctx);
            }
        }
    }

    /**
     * 컨트롤러와 매칭되는 url 없음
     */
    private void sendNotFoundResponse(ChannelHandlerContext ctx) {
        responseMessage(
                ctx,
                FullHttpResponseBuilder.builder()
                        .body("The requested resource was not found.")
                        .statusCode(HttpResponseStatus.NOT_FOUND)
                        .build()
        );
    }

    /**
     * 비동기적 응답이 {@link FullHttpAsyncResponse} 아니다. 처리할 수 없음.
     */
    private void sendErrorResponse(ChannelHandlerContext ctx) {
        responseMessage(
                ctx,
                FullHttpResponseBuilder.builder()
                        .body("Error occurred.")
                        .statusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR)
                        .build()
        );
    }

    private void responseMessage(ChannelHandlerContext ctx, Object msg) {
        ctx.writeAndFlush(msg);
    }
}
