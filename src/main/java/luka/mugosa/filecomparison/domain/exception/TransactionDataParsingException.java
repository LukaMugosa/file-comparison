package luka.mugosa.filecomparison.domain.exception;

public class TransactionDataParsingException extends FileParsingException {

    public TransactionDataParsingException(String message, Throwable cause) {
        super("Failed to parse transaction data: " + message, cause);
    }
}
