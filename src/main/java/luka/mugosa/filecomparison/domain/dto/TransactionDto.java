package luka.mugosa.filecomparison.domain.dto;

import luka.mugosa.filecomparison.domain.enumeration.TransactionType;
import luka.mugosa.filecomparison.domain.id.TransactionId;

import java.io.Serializable;
import java.time.ZonedDateTime;

public class TransactionDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String profileName;
    private final ZonedDateTime transactionDate;
    private final Double transactionAmount;
    private final String transactionNarrative;
    private final String transactionDescription;
    private final TransactionId transactionID;
    private final TransactionType transactionType;
    private final String walletReference;

    public TransactionDto(String profileName, ZonedDateTime transactionDate, Double transactionAmount, String transactionNarrative, String transactionDescription, TransactionId transactionID, TransactionType transactionType, String walletReference) {
        this.profileName = profileName;
        this.transactionDate = transactionDate;
        this.transactionAmount = transactionAmount;
        this.transactionNarrative = transactionNarrative;
        this.transactionDescription = transactionDescription;
        this.transactionID = transactionID;
        this.transactionType = transactionType;
        this.walletReference = walletReference;
    }

    public String getProfileName() {
        return profileName;
    }

    public ZonedDateTime getTransactionDate() {
        return transactionDate;
    }

    public Double getTransactionAmount() {
        return transactionAmount;
    }

    public String getTransactionNarrative() {
        return transactionNarrative;
    }

    public String getTransactionDescription() {
        return transactionDescription;
    }

    public TransactionId getTransactionID() {
        return transactionID;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public String getWalletReference() {
        return walletReference;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        TransactionDto that = (TransactionDto) o;
        return profileName.equals(that.profileName) && transactionDate.equals(that.transactionDate) && transactionAmount.equals(that.transactionAmount) && transactionNarrative.equals(that.transactionNarrative) && transactionDescription == that.transactionDescription && transactionID.equals(that.transactionID) && transactionType == that.transactionType && walletReference.equals(that.walletReference);
    }

    @Override
    public int hashCode() {
        int result = profileName.hashCode();
        result = 31 * result + transactionDate.hashCode();
        result = 31 * result + transactionAmount.hashCode();
        result = 31 * result + transactionNarrative.hashCode();
        result = 31 * result + transactionDescription.hashCode();
        result = 31 * result + transactionID.hashCode();
        result = 31 * result + transactionType.hashCode();
        result = 31 * result + walletReference.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "TransactionDto{" +
                "profileName='" + profileName + '\'' +
                ", transactionDate=" + transactionDate +
                ", transactionAmount=" + transactionAmount +
                ", transactionNarrative='" + transactionNarrative + '\'' +
                ", transactionDescription=" + transactionDescription +
                ", transactionID=" + transactionID +
                ", transactionType=" + transactionType +
                ", walletReference='" + walletReference + '\'' +
                '}';
    }
}
