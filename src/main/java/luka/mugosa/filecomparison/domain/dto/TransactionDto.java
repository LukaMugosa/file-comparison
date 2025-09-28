package luka.mugosa.filecomparison.domain.dto;

import luka.mugosa.filecomparison.domain.id.TransactionId;
import luka.mugosa.filecomparison.domain.enumeration.TransactionType;

import java.time.ZonedDateTime;
import java.util.Objects;

public class TransactionDto {

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
        return Objects.equals(profileName, that.profileName) && Objects.equals(transactionDate, that.transactionDate) && Objects.equals(transactionAmount, that.transactionAmount) && Objects.equals(transactionNarrative, that.transactionNarrative) && Objects.equals(transactionDescription, that.transactionDescription) && Objects.equals(transactionID, that.transactionID) && transactionType == that.transactionType && Objects.equals(walletReference, that.walletReference);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(profileName);
        result = 31 * result + Objects.hashCode(transactionDate);
        result = 31 * result + Objects.hashCode(transactionAmount);
        result = 31 * result + Objects.hashCode(transactionNarrative);
        result = 31 * result + Objects.hashCode(transactionDescription);
        result = 31 * result + Objects.hashCode(transactionID);
        result = 31 * result + Objects.hashCode(transactionType);
        result = 31 * result + Objects.hashCode(walletReference);
        return result;
    }

    @Override
    public String toString() {
        return "TransactionDto{" +
                "profileName='" + profileName + '\'' +
                ", transactionDate=" + transactionDate +
                ", transactionAmount=" + transactionAmount +
                ", transactionNarrative='" + transactionNarrative + '\'' +
                ", transactionDescription='" + transactionDescription + '\'' +
                ", transactionID='" + transactionID + '\'' +
                ", transactionType=" + transactionType +
                ", walletReference='" + walletReference + '\'' +
                '}';
    }
}
