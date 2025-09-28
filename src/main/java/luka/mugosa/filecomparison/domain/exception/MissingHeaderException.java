package luka.mugosa.filecomparison.domain.exception;

public class MissingHeaderException extends FileParsingException {
    
    private final String missingHeader;
    
    public MissingHeaderException(String missingHeader) {
        super("Missing required header: " + missingHeader);
        this.missingHeader = missingHeader;
    }
    
    public String getMissingHeader() {
        return missingHeader;
    }
}