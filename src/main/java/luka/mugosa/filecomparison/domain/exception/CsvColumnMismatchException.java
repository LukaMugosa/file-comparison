package luka.mugosa.filecomparison.domain.exception;

public class CsvColumnMismatchException extends FileParsingException {
    
    private final int expectedColumns;
    private final int actualColumns;
    
    public CsvColumnMismatchException(int expectedColumns, int actualColumns) {
        super(String.format("Expected %d columns but found %d", expectedColumns, actualColumns));
        this.expectedColumns = expectedColumns;
        this.actualColumns = actualColumns;
    }
    
    public int getExpectedColumns() {
        return expectedColumns;
    }
    
    public int getActualColumns() {
        return actualColumns;
    }
}