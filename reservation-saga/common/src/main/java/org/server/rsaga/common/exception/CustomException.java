package org.server.rsaga.common.exception;

import lombok.Getter;

@Getter
public class CustomException extends RuntimeException{
    private final ErrorCode errorCode;

    public CustomException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public CustomException(String errorCode, String errorMessage) {
        super(errorMessage);
        this.errorCode = matchErrorCode(errorCode);
    }

    /**
     * 메시지로 에러를 전달하면 String 으로 전달하기 때문에, 에러 코드 매칭이 필요하다.
     */
    private ErrorCode matchErrorCode(String errorCode) {
        for (ErrorCode code : ErrorCode.values()) {
            if (code.getCode().equals(errorCode)) {
                return code;
            }
        }

        return ErrorCode.PROCESSING_ERROR;
    }
}
