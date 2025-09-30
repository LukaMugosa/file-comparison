package luka.mugosa.filecomparison.domain.dto;

import java.io.Serializable;
import java.util.Objects;

public final class UnmatchedTransactionPairDto implements Serializable {

    private static final long serialVersionUID = 1L;

    final TransactionDto transaction1;
    final TransactionDto transaction2;

    public UnmatchedTransactionPairDto(TransactionDto transaction1, TransactionDto transaction2) {
        this.transaction1 = transaction1;
        this.transaction2 = transaction2;
    }

    public TransactionDto getTransaction1() {
        return transaction1;
    }

    public TransactionDto getTransaction2() {
        return transaction2;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        UnmatchedTransactionPairDto that = (UnmatchedTransactionPairDto) o;
        return Objects.equals(transaction1, that.transaction1) && Objects.equals(transaction2, that.transaction2);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(transaction1);
        result = 31 * result + Objects.hashCode(transaction2);
        return result;
    }

    @Override
    public String toString() {
        return "UnmatchedTransactionPairDto{" +
                "transaction1=" + transaction1 +
                ", transaction2=" + transaction2 +
                '}';
    }
}
