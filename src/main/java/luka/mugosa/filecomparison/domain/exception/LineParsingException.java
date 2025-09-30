package luka.mugosa.filecomparison.domain.exception;

public class LineParsingException extends FileParsingException {

    private final int lineNumber;
    private final String lineContent;

    public LineParsingException(int lineNumber, String message, String lineContent, Throwable cause) {
        super(String.format("Error parsing line %d: %s. Line content: %s", lineNumber, message, lineContent), cause);
        this.lineNumber = lineNumber;
        this.lineContent = lineContent;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public String getLineContent() {
        return lineContent;
    }
}