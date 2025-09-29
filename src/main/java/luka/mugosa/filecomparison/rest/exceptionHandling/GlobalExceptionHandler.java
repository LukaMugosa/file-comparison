package luka.mugosa.filecomparison.rest.exceptionHandling;

import luka.mugosa.filecomparison.domain.dto.response.ErrorResponseDto;
import luka.mugosa.filecomparison.domain.enumeration.ErrorType;
import luka.mugosa.filecomparison.domain.exception.CsvColumnMismatchException;
import luka.mugosa.filecomparison.domain.exception.DateParsingException;
import luka.mugosa.filecomparison.domain.exception.EmptyFileException;
import luka.mugosa.filecomparison.domain.exception.FileParsingException;
import luka.mugosa.filecomparison.domain.exception.FileProcessingException;
import luka.mugosa.filecomparison.domain.exception.InvalidHeaderException;
import luka.mugosa.filecomparison.domain.exception.LineParsingException;
import luka.mugosa.filecomparison.domain.exception.MissingHeaderException;
import luka.mugosa.filecomparison.domain.exception.TransactionDataParsingException;
import luka.mugosa.filecomparison.domain.exception.TransactionTypeParsingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(EmptyFileException.class)
    public ResponseEntity<ErrorResponseDto> handleEmptyFileException(final EmptyFileException ex) {
        logger.warn("Empty file provided: {}", ex.getMessage());

        final ErrorResponseDto response = new ErrorResponseDto(ErrorType.EMPTY_FILE, ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(MissingHeaderException.class)
    public ResponseEntity<ErrorResponseDto> handleMissingHeaderException(final MissingHeaderException ex) {
        logger.warn("Missing required header: {}", ex.getMissingHeader());

        final String message = "Required CSV header is missing: " + ex.getMissingHeader();
        final ErrorResponseDto response = new ErrorResponseDto(ErrorType.MISSING_HEADER, message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(InvalidHeaderException.class)
    public ResponseEntity<ErrorResponseDto> handleInvalidHeaderException(final InvalidHeaderException ex) {
        logger.warn("Invalid header: {}", ex.getHeaderName());

        final ErrorResponseDto response = new ErrorResponseDto(ErrorType.INVALID_HEADER, ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(CsvColumnMismatchException.class)
    public ResponseEntity<ErrorResponseDto> handleCsvColumnMismatchException(final CsvColumnMismatchException ex) {
        logger.warn("CSV column mismatch - Expected: {}, Actual: {}", ex.getExpectedColumns(), ex.getActualColumns());

        final String message = String.format("Expected %d columns but found %d",
                ex.getExpectedColumns(), ex.getActualColumns());
        final ErrorResponseDto response = new ErrorResponseDto(ErrorType.COLUMN_MISMATCH, message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(LineParsingException.class)
    public ResponseEntity<ErrorResponseDto> handleLineParsingException(final LineParsingException ex) {
        logger.error("Error parsing line {}: {}", ex.getLineNumber(), ex.getMessage());

        final String message = String.format("Error parsing CSV at line %d", ex.getLineNumber());
        final ErrorResponseDto response = new ErrorResponseDto(ErrorType.LINE_PARSING_ERROR, message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(DateParsingException.class)
    public ResponseEntity<ErrorResponseDto> handleDateParsingException(final DateParsingException ex) {
        logger.warn("Date parsing error for value: {}", ex.getDateValue());

        final ErrorResponseDto response = new ErrorResponseDto(ErrorType.DATE_PARSING_ERROR, ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(TransactionTypeParsingException.class)
    public ResponseEntity<ErrorResponseDto> handleTransactionTypeParsingException(final TransactionTypeParsingException ex) {
        logger.warn("Transaction type parsing error for value: {}", ex.getTypeValue());

        final ErrorResponseDto response = new ErrorResponseDto(ErrorType.TRANSACTION_TYPE_ERROR, ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(TransactionDataParsingException.class)
    public ResponseEntity<ErrorResponseDto> handleTransactionDataParsingException(final TransactionDataParsingException ex) {
        logger.error("Transaction data parsing error: {}", ex.getMessage(), ex);

        final ErrorResponseDto response = new ErrorResponseDto(ErrorType.TRANSACTION_DATA_ERROR, ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(FileProcessingException.class)
    public ResponseEntity<ErrorResponseDto> handleFileProcessingException(final FileProcessingException ex) {
        logger.error("File processing error: {}", ex.getMessage(), ex);

        final ErrorResponseDto response = new ErrorResponseDto(ErrorType.FILE_PROCESSING_ERROR, ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler(FileParsingException.class)
    public ResponseEntity<ErrorResponseDto> handleFileParsingException(final FileParsingException ex) {
        logger.error("File parsing error: {}", ex.getMessage(), ex);

        final ErrorResponseDto response = new ErrorResponseDto(ErrorType.FILE_PARSING_ERROR, ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponseDto> handleMaxUploadSizeExceeded(final MaxUploadSizeExceededException ex) {
        logger.warn("File upload size exceeded: {}", ex.getMessage());

        final ErrorResponseDto response = new ErrorResponseDto(ErrorType.FILE_SIZE_EXCEEDED,
                "Please upload a smaller file");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseDto> handleIllegalArgumentException(final IllegalArgumentException ex) {
        logger.warn("Invalid argument: {}", ex.getMessage());

        final ErrorResponseDto response = new ErrorResponseDto(ErrorType.INVALID_ARGUMENT, ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponseDto> handleRuntimeException(final RuntimeException ex) {
        logger.error("Unexpected runtime error: {}", ex.getMessage(), ex);

        final ErrorResponseDto response = new ErrorResponseDto(ErrorType.RUNTIME_ERROR,
                "Please try again or contact support if the problem persists");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleGenericException(final Exception ex) {
        logger.error("Unexpected error: {}", ex.getMessage(), ex);

        final ErrorResponseDto response = new ErrorResponseDto(ErrorType.INTERNAL_ERROR,
                "Please contact support if the problem persists");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}