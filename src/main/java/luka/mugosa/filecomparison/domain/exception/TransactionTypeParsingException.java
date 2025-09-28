package luka.mugosa.filecomparison.domain.exception;

public class TransactionTypeParsingException extends FileParsingException {

    private final String typeValue;

    public TransactionTypeParsingException(String typeValue) {
        super("Transaction type cannot be empty");
        this.typeValue = typeValue;
    }

    public String getTypeValue() {
        return typeValue;
    }
}