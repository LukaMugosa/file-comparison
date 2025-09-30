package luka.mugosa.filecomparison.domain.exception;

public class DateParsingException extends FileParsingException {

    private final String dateValue;

    public DateParsingException(String dateValue) {
        super("Transaction date cannot be empty");
        this.dateValue = dateValue;
    }

    public DateParsingException(String dateValue, String message) {
        super(message);
        this.dateValue = dateValue;
    }

    public String getDateValue() {
        return dateValue;
    }
}