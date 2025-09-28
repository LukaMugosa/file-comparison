package luka.mugosa.filecomparison.domain.exception;

public class InvalidHeaderException extends FileParsingException {
    
    private final String headerName;
    
    public InvalidHeaderException(String headerName) {
        super("Header not found or invalid: " + headerName);
        this.headerName = headerName;
    }
    
    public String getHeaderName() {
        return headerName;
    }
}