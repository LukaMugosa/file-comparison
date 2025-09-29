package luka.mugosa.filecomparison.service.util;

import luka.mugosa.filecomparison.domain.dto.TransactionDto;
import luka.mugosa.filecomparison.domain.enumeration.TransactionType;
import luka.mugosa.filecomparison.domain.id.TransactionId;
import luka.mugosa.filecomparison.domain.score.dto.MatchConfidence;
import luka.mugosa.filecomparison.domain.score.dto.MatchScore;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
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
                "REVERSAL",
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
                "REVERSAL",
                new TransactionId(id),
                TransactionType.TYPE_1,
                "WalletRef"
        );
    }

    public static List<TransactionDto> createTransactionList(TransactionDto... transactions) {
        return new ArrayList<>(Arrays.asList(transactions));
    }

    public static MatchScore createMatchScore(double score, MatchConfidence confidence) {
        final Map<String, Double> componentScores = new HashMap<>();
        componentScores.put("TestComponent", score);
        return new MatchScore(score, confidence, componentScores);
    }

    public static List<TransactionDto> createTransactionSet(String... ids) {
        final List<TransactionDto> set = new ArrayList<>();
        for (String id : ids) {
            set.add(createTransaction(id));
        }
        return set;
    }

    public static List<TransactionDto> createLargeTransactionSet(int size) {
        final List<TransactionDto> set = new ArrayList<>();
        for (int i = 1; i <= size; i++) {
            set.add(createTransaction("TXN" + String.format("%04d", i)));
        }
        return set;
    }

    public static TransactionDto createTransaction(String id) {
        return new TransactionDto(
                "ProfileName",
                ZonedDateTime.now(),
                100.0,
                "Narrative",
                "REVERSAL",
                new TransactionId(id),
                null,
                "WalletRef"
        );
    }
}
