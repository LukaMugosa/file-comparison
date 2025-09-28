package luka.mugosa.filecomparison.domain.enumeration;

// Error Type Enum
public enum ErrorType {
    EMPTY_FILE("The provided file is empty"),
    MISSING_HEADER("Required CSV header is missing"),
    INVALID_HEADER("Invalid or missing header in CSV file"),
    COLUMN_MISMATCH("CSV format error: column count mismatch"),
    LINE_PARSING_ERROR("Error parsing CSV line"),
    DATE_PARSING_ERROR("Invalid date format in transaction data"),
    TRANSACTION_TYPE_ERROR("Invalid transaction type in data"),
    TRANSACTION_DATA_ERROR("Error parsing transaction data"),
    FILE_PROCESSING_ERROR("Error processing file"),
    FILE_PARSING_ERROR("Error parsing file"),
    FILE_SIZE_EXCEEDED("File size exceeds maximum allowed limit"),
    INVALID_ARGUMENT("Invalid request parameters"),
    RUNTIME_ERROR("An unexpected error occurred"),
    INTERNAL_ERROR("An internal server error occurred");

    private final String defaultMessage;

    ErrorType(String defaultMessage) {
        this.defaultMessage = defaultMessage;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }
}