package org.server.rsaga.netty.http.handler;

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.server.rsaga.common.dto.ErrorResponse;
import org.server.rsaga.common.dto.FullHttpResponseBuilder;
import org.server.rsaga.common.exception.CustomException;
import org.server.rsaga.common.exception.ErrorCode;

import java.lang.reflect.InvocationTargetException;
import java.util.stream.Collectors;

// TODO : Exception Handler 구현, ControllerAdvice
@Slf4j
public class ExceptionHandler extends ChannelHandlerAdapter {

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        if(cause instanceof InvocationTargetException invocationTargetException){
            cause = invocationTargetException.getTargetException();
        }
        log.error("Exception, {}", cause.getMessage());

        FullHttpResponse response = exceptionResponse(cause);

        ctx.writeAndFlush(response);
    }

    private FullHttpResponse exceptionResponse(Throwable cause) {
        FullHttpResponseBuilder responseBuilder = FullHttpResponseBuilder.builder();
        if (cause instanceof CustomException customException) {
            ErrorCode errorCode = customException.getErrorCode();

            ErrorResponse errorResponse = ErrorResponse.of(
                    errorCode.getCode(),
                    errorCode.getMessage()
            );

            responseBuilder = responseBuilder
                    .body(errorResponse)
                    .statusCode(errorCode.getStatus());

        } else if (cause instanceof ConstraintViolationException constraintViolationException) {
            String errorMessage = constraintViolationException.getConstraintViolations().stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining(", "));

            ErrorResponse errorResponse = ErrorResponse.of(
                    ErrorCode.BAD_REQUEST_BODY.getCode(),
                    errorMessage
            );

            responseBuilder = responseBuilder
                    .body(errorResponse)
                    .statusCode(HttpResponseStatus.BAD_REQUEST);
        }

        else {
            // todo : 수정해야함.
            ErrorResponse errorResponse = ErrorResponse.of(
                    "000",
                    "Internal Server Error. " + cause.getMessage()
            );

            responseBuilder = responseBuilder
                    .body(errorResponse)
                    .statusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR);
        }

        return responseBuilder.build();
    }
}
