package luka.mugosa.filecomparison.domain.exception;

public class EmptyFileException extends FileParsingException {
    
    public EmptyFileException(String message) {
        super(message);
    }
}