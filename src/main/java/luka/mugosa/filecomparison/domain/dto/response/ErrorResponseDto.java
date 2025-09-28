package luka.mugosa.filecomparison.domain.dto.response;

import luka.mugosa.filecomparison.domain.enumeration.ErrorType;

import java.io.Serializable;

public final class ErrorResponseDto implements Serializable {

    private final ErrorType errorType;
    private final String message;

    public ErrorResponseDto(ErrorType errorType, String message) {
        this.errorType = errorType;
        this.message = message;
    }

    // Getters and Setters
    public ErrorType getErrorType() {
        return errorType;
    }

    public String getMessage() {
        return message;
    }

}