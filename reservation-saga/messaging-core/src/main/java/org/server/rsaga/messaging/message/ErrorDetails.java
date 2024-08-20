package org.server.rsaga.messaging.message;

public interface ErrorDetails {
    String ERROR_CODE = "ERROR_CODE";
    String ERROR_MESSAGE = "ERROR_MESSAGE";

    String errorCode();
    String errorMessage();
}
