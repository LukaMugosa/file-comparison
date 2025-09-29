package luka.mugosa.filecomparison.domain.dto.response;

import luka.mugosa.filecomparison.domain.enumeration.ErrorType;

import java.io.Serializable;

public record ErrorResponseDto(ErrorType errorType, String message) implements Serializable {
}