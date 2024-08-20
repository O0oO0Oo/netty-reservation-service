package org.server.rsaga.saga.exception;

public class RemoteServiceException extends RuntimeException{
    private final String errorCode;
    private final String errorMessage;

    public RemoteServiceException(String errorCode, String errorMessage) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    @Override
    public String getMessage() {
        return errorCode + " " + errorMessage;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
