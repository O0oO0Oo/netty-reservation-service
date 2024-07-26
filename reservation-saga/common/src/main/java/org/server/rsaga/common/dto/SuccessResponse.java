package org.server.rsaga.common.dto;

import io.netty.handler.codec.http.HttpResponseStatus;
import jakarta.validation.constraints.NotNull;

public record SuccessResponse(
        @NotNull
        int code,
        @NotNull
        Object message
) {
    public static SuccessResponse of(HttpResponseStatus status, Object message) {
        return new SuccessResponse(status.code(), message);
    }
}
