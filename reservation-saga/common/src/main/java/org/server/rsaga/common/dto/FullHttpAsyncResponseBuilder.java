package org.server.rsaga.common.dto;

import io.netty.handler.codec.AsciiString;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.Promise;
import jakarta.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class FullHttpAsyncResponseBuilder {
    private HttpVersion httpVersion = HttpVersion.HTTP_1_1;
    private HttpResponseStatus httpResponseStatus = HttpResponseStatus.OK;
    private final Map<AsciiString, String> headerMap = new HashMap<>();
    private final Promise<?> asyncResult;

    public FullHttpAsyncResponseBuilder(Promise<?> asyncResult) {
        checkNull(asyncResult);
        this.asyncResult = asyncResult;
    }

    /**
     * 비동기 응답을 위한 객체는 필수여야 한다.
     * @param result 비동기 응답
     * @return {@link FullHttpAsyncResponseBuilder}
     */
    public static FullHttpAsyncResponseBuilder builder(Promise<?> result) {
        return new FullHttpAsyncResponseBuilder(result);
    }

    public FullHttpAsyncResponseBuilder httpVersion(HttpVersion httpVersion) {
        this.httpVersion = httpVersion;
        return this;
    }

    public FullHttpAsyncResponseBuilder statusCode(HttpResponseStatus statusCode) {
        this.httpResponseStatus = statusCode;
        return this;
    }

    public FullHttpAsyncResponseBuilder addHeader(@Nonnull AsciiString headerName, String value) {
        this.headerMap.put(headerName, value);
        return this;
    }

    public FullHttpAsyncResponse build() {
        checkNull(this.asyncResult);
        return new FullHttpAsyncResponse(httpVersion, httpResponseStatus, headerMap, asyncResult);
    }

    private void checkNull(Object object) {
        if (object == null) {
            throw new IllegalArgumentException("Async result (Promise<?>) cannot be null.");
        }
    }
}
