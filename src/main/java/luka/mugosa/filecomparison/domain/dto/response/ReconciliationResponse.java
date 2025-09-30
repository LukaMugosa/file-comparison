package luka.mugosa.filecomparison.domain.dto.response;

import luka.mugosa.filecomparison.domain.dto.UnmatchedTransactionPairDto;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;

public record ReconciliationResponse(int totalRecordsInFile1, int totalRecordsInFile2, int unmatchedRecordsInFile1,
                                     int unmatchedRecordsInFile2, int matchedRecords,
                                     List<UnmatchedTransactionPairDto> unmatchedTransactionPairs,
                                     double matchPercentage) implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        ReconciliationResponse that = (ReconciliationResponse) o;
        return totalRecordsInFile1 == that.totalRecordsInFile1 && totalRecordsInFile2 == that.totalRecordsInFile2 && unmatchedRecordsInFile1 == that.unmatchedRecordsInFile1 && unmatchedRecordsInFile2 == that.unmatchedRecordsInFile2 && matchedRecords == that.matchedRecords && Double.compare(matchPercentage, that.matchPercentage) == 0 && Objects.equals(unmatchedTransactionPairs, that.unmatchedTransactionPairs);
    }

    @Override
    public int hashCode() {
        int result = totalRecordsInFile1;
        result = 31 * result + totalRecordsInFile2;
        result = 31 * result + unmatchedRecordsInFile1;
        result = 31 * result + unmatchedRecordsInFile2;
        result = 31 * result + matchedRecords;
        result = 31 * result + Objects.hashCode(unmatchedTransactionPairs);
        result = 31 * result + Double.hashCode(matchPercentage);
        return result;
    }

    @Override
    public String toString() {
        return "ReconciliationResponse{" +
                "totalRecordsInFile1=" + totalRecordsInFile1 +
                ", totalRecordsInFile2=" + totalRecordsInFile2 +
                ", unmatchedRecordsInFile1=" + unmatchedRecordsInFile1 +
                ", unmatchedRecordsInFile2=" + unmatchedRecordsInFile2 +
                ", matchedRecords=" + matchedRecords +
                ", unmatchedTransactionPairs=" + unmatchedTransactionPairs +
                ", matchPercentage=" + matchPercentage +
                '}';
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private int totalRecordsInFile1;
        private int totalRecordsInFile2;
        private int unmatchedRecordsInFile1;
        private int unmatchedRecordsInFile2;
        private int matchedRecords;
        private double matchPercentage;
        private List<UnmatchedTransactionPairDto> unmatchedTransactionPairs;

        public Builder totalRecordsInFile1(int totalRecordsInFile1) {
            this.totalRecordsInFile1 = totalRecordsInFile1;
            return this;
        }

        public Builder totalRecordsInFile2(int totalRecordsInFile2) {
            this.totalRecordsInFile2 = totalRecordsInFile2;
            return this;
        }

        public Builder unmatchedRecordsInFile1(int unmatchedRecordsInFile1) {
            this.unmatchedRecordsInFile1 = unmatchedRecordsInFile1;
            return this;
        }

        public Builder unmatchedRecordsInFile2(int unmatchedRecordsInFile2) {
            this.unmatchedRecordsInFile2 = unmatchedRecordsInFile2;
            return this;
        }

        public Builder matchedRecords(int matchedRecords) {
            this.matchedRecords = matchedRecords;
            return this;
        }

        public Builder unmatchedTransactionPairs(List<UnmatchedTransactionPairDto> unmatchedTransactionPairs) {
            this.unmatchedTransactionPairs = unmatchedTransactionPairs;
            return this;
        }

        public Builder matchPercentage(double matchPercentage) {
            this.matchPercentage = matchPercentage;
            return this;
        }

        public ReconciliationResponse build() {
            return new ReconciliationResponse(totalRecordsInFile1, totalRecordsInFile2,
                    unmatchedRecordsInFile1, unmatchedRecordsInFile2, matchedRecords, unmatchedTransactionPairs, matchPercentage);
        }
    }


}
