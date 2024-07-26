package org.server.reservation.core.common.dto;

import jakarta.validation.constraints.NotNull;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

import java.util.List;

public record ErrorResponse(
        @NotNull
        String errorCode,
        @NotNull
        String errorMessage
) {

    public static ErrorResponse of(String errorCode, String errorMessage) {
        return new ErrorResponse(errorCode, errorMessage);
    }

    public static ErrorResponse of(String errorCode, BindingResult bindingResult) {
        return new ErrorResponse(errorCode, createErrorMessage(bindingResult));
    }

    private static String createErrorMessage(BindingResult bindingResult) {
        StringBuilder sb = new StringBuilder();

        List<ObjectError> allErrors = bindingResult.getAllErrors();
        for (int i = 0; i < allErrors.size(); i++) {
            ObjectError error = allErrors.get(i);

            if (error instanceof FieldError fieldError) {
                sb.append("[");
                sb.append(fieldError.getField());
                sb.append("] ");
                sb.append(fieldError.getDefaultMessage());
            }
            else {
                sb.append(error.getDefaultMessage());
            }

            if (i < allErrors.size() - 1) {
                sb.append(", ");
            }
        }

        return sb.toString();
    }
}
