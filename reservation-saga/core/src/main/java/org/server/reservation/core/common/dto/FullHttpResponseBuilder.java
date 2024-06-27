package org.server.reservation.core.common.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.AsciiString;
import io.netty.handler.codec.http.*;
import jakarta.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Slf4j
public class FullHttpResponseBuilder {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private ByteBuf responseBody;
    private HttpVersion httpVersion = HttpVersion.HTTP_1_1;
    private HttpResponseStatus httpResponseStatus = HttpResponseStatus.OK;
    private Map<AsciiString, String> headerMap;

    public static FullHttpResponseBuilder builder() {
        FullHttpResponseBuilder fullHttpResponseBuilder = new FullHttpResponseBuilder();
        fullHttpResponseBuilder.headerMap = new HashMap<>();
        fullHttpResponseBuilder.headerMap.put(HttpHeaderNames.CONTENT_TYPE, "json; charset=UTF-8");
        return fullHttpResponseBuilder;
    }

    public FullHttpResponseBuilder body(Object responseBody) {
        try {
            Object nonNullBody = bodyNonNullOrElseDefault(responseBody);
            Object responseWrapper = wrapAsSuccessResponseIfNotError(nonNullBody);
            String jsonBody = objectMapper.writeValueAsString(responseWrapper);
            this.responseBody = Unpooled.copiedBuffer(jsonBody, StandardCharsets.UTF_8);
        } catch (JsonProcessingException e) {
            log.error(e.getMessage());
            // TODO : 예외 처리
        }
        return this;
    }

    private Object bodyNonNullOrElseDefault(Object responseBody) {
        return Objects.requireNonNullElse(responseBody, "");
    }

    private Object wrapAsSuccessResponseIfNotError(Object nonNullBody) {
        if(nonNullBody instanceof ErrorResponse){
            return nonNullBody;
        }
        else {
            return SuccessResponse.of(httpResponseStatus, nonNullBody);
        }
    }

    public FullHttpResponseBuilder httpVersion(HttpVersion httpVersion) {
        this.httpVersion = httpVersion;
        return this;
    }

    public FullHttpResponseBuilder statusCode(HttpResponseStatus statusCode) {
        this.httpResponseStatus = statusCode;
        return this;
    }

    public FullHttpResponseBuilder addHeader(@Nonnull AsciiString headerName, String value) {
        this.headerMap.put(headerName, value);
        return this;
    }

    public FullHttpResponse build() {
        DefaultFullHttpResponse response = new DefaultFullHttpResponse(httpVersion, httpResponseStatus, responseBody);
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, String.valueOf(responseBody.readableBytes()));
        headerMap.forEach((key, val) -> response.headers().set(key, val));
        return response;
    }
}