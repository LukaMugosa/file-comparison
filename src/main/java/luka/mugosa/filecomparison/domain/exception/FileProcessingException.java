package luka.mugosa.filecomparison.domain.exception;

public class FileProcessingException extends FileParsingException {

    public FileProcessingException(String message, Throwable cause) {
        super("Failed to process file: " + message, cause);
    }
}