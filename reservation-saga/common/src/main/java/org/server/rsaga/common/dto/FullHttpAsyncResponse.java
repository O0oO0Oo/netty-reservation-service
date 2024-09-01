package org.server.rsaga.common.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.AsciiString;
import io.netty.handler.codec.http.*;
import io.netty.util.concurrent.Promise;

import java.nio.charset.StandardCharsets;
import java.util.Map;

public class FullHttpAsyncResponse {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpVersion httpVersion;
    private final HttpResponseStatus httpResponseStatus;
    private final Map<AsciiString, String> headerMap;
    private final Promise<?> asyncResult;

    public FullHttpAsyncResponse(HttpVersion httpVersion,
                                 HttpResponseStatus httpResponseStatus,
                                 Map<AsciiString, String> headerMap,
                                 Promise<?> asyncResult) {
        this.httpVersion = httpVersion;
        this.httpResponseStatus = httpResponseStatus;
        this.headerMap = headerMap;
        this.asyncResult = asyncResult;
    }

    /**
     * Promise 가 완료되었을 때 ctx 로 응답을 줄지, 에러처리를 할지 정하게 된다.
     * @param ctx
     */
    public void processResult(ChannelHandlerContext ctx) {
        asyncResult.addListener(
                result -> {
                    if (result.isSuccess()) {
                        ByteBuf responseBody = buildBody(result.get());

                        FullHttpResponse response = buildFullHttpResponse(responseBody);
                        ctx.writeAndFlush(response);
                    }
                    else {
                        ctx.fireExceptionCaught(result.cause());
                    }
                }
        );
    }

    private ByteBuf buildBody(Object result) throws JsonProcessingException {
        String jsonBody = objectMapper.writeValueAsString(result);
        return Unpooled.copiedBuffer(jsonBody, StandardCharsets.UTF_8);
    }

    private FullHttpResponse buildFullHttpResponse(ByteBuf responseBody) {
        DefaultFullHttpResponse response = new DefaultFullHttpResponse(httpVersion, httpResponseStatus, responseBody);
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json");
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, String.valueOf(responseBody.readableBytes()));
        headerMap.forEach((key, val) -> response.headers().set(key, val));
        return response;
    }
}
