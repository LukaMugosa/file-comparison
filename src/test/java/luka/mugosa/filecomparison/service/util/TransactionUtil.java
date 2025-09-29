package luka.mugosa.filecomparison.service.util;

import luka.mugosa.filecomparison.domain.dto.TransactionDto;
import luka.mugosa.filecomparison.domain.enumeration.TransactionType;
import luka.mugosa.filecomparison.domain.id.TransactionId;
import luka.mugosa.filecomparison.domain.score.dto.MatchConfidence;
import luka.mugosa.filecomparison.domain.score.dto.MatchScore;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TransactionUtil {
    // Helper methods to create test transactions
    public static TransactionDto createTransaction(String id, Double amount, ZonedDateTime date, String profileName) {
        return new TransactionDto(
                profileName,
                date,
                amount,
                null,
                null,
                id != null ? new TransactionId(id) : null,
                null,
                null
        );
    }
    // Helper methods to create test transactions
    public static TransactionDto createTransaction(String id, Double amount, ZonedDateTime date) {
        return new TransactionDto(
                "ProfileName",
                date,
                amount,
                null,
                null,
                id != null ? new TransactionId(id) : null,
                null,
                null
        );
    }

    public static TransactionDto createTransactionWithNarrative(String id, String narrative) {
        return new TransactionDto(
                "ProfileName",
                ZonedDateTime.now(),
                100.0,
                narrative,
                null,
                new TransactionId(id),
                null,
                null
        );
    }

    public static TransactionDto createTransactionWithWallet(String id, String wallet) {
        return new TransactionDto(
                "ProfileName",
                ZonedDateTime.now(),
                100.0,
                null,
                null,
                new TransactionId(id),
                null,
                wallet
        );
    }

    public static TransactionDto createTransactionWithType(String id, TransactionType type) {
        return new TransactionDto(
                "ProfileName",
                ZonedDateTime.now(),
                100.0,
                null,
                null,
                id != null ? new TransactionId(id) : null,
                type,
                null
        );
    }

    public static TransactionDto createPerfectMatchTransaction() {
        final ZonedDateTime date = ZonedDateTime.now();
        return new TransactionDto(
                "ProfileName",
                date,
                100.0,
                "PAYMENT TO STORE",
                "Description",
                new TransactionId("TXN001"),
                TransactionType.TYPE_1,
                "WALLET123"
        );
    }

    public static TransactionDto createTransactionTwoMainParams(String id, Double amount) {
        return new TransactionDto(
                "ProfileName",
                ZonedDateTime.now(),
                amount,
                "Narrative",
                "Description",
                new TransactionId(id),
                TransactionType.TYPE_1,
                "WalletRef"
        );
    }

    public static List<TransactionDto> createTransactionList(TransactionDto... transactions) {
        final List<TransactionDto> list = new ArrayList<>();
        for (TransactionDto txn : transactions) {
            list.add(txn);
        }
        return list;
    }

    public static MatchScore createMatchScore(double score, MatchConfidence confidence) {
        final Map<String, Double> componentScores = new HashMap<>();
        componentScores.put("TestComponent", score);
        return new MatchScore(score, confidence, componentScores);
    }
}
