package luka.mugosa.filecomparison.service.score;

import luka.mugosa.filecomparison.domain.dto.TransactionDto;
import luka.mugosa.filecomparison.domain.enumeration.TransactionType;
import luka.mugosa.filecomparison.domain.id.TransactionId;
import luka.mugosa.filecomparison.domain.score.dto.MatchConfidence;
import luka.mugosa.filecomparison.domain.score.dto.MatchScore;
import luka.mugosa.filecomparison.domain.score.dto.ScoringWeights;
import luka.mugosa.filecomparison.service.ScoreService;
import org.apache.commons.text.similarity.JaroWinklerSimilarity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static luka.mugosa.filecomparison.constant.TransactionConstants.HEADER_PROFILE_NAME;
import static luka.mugosa.filecomparison.constant.TransactionConstants.HEADER_TRANSACTION_AMOUNT;
import static luka.mugosa.filecomparison.constant.TransactionConstants.HEADER_TRANSACTION_DATE;
import static luka.mugosa.filecomparison.constant.TransactionConstants.HEADER_TRANSACTION_DESCRIPTION;
import static luka.mugosa.filecomparison.constant.TransactionConstants.HEADER_TRANSACTION_ID;
import static luka.mugosa.filecomparison.constant.TransactionConstants.HEADER_TRANSACTION_NARRATIVE;
import static luka.mugosa.filecomparison.constant.TransactionConstants.HEADER_TRANSACTION_TYPE;
import static luka.mugosa.filecomparison.constant.TransactionConstants.HEADER_WALLET_REFERENCE;

@Service
public class ScoreServiceImpl implements ScoreService {

    // Tolerance thresholds
    private static final double AMOUNT_TOLERANCE_PERCENTAGE = 0.01;    // 1%
    private static final int DATE_TOLERANCE_DAYS = 2;                  // ±2 days
    private static final double STRING_SIMILARITY_THRESHOLD = 0.7;     // 70% similarity

    final JaroWinklerSimilarity jaroWinkler = new JaroWinklerSimilarity();

    /**
     * Calculate a comprehensive matching score between two transactions
     */
    public MatchScore calculateScore(TransactionDto transaction1, TransactionDto transaction2) {
        final Map<String, Double> componentScores = new HashMap<>();

        final double idScore = scoreTransactionId(transaction1.getTransactionID(), transaction2.getTransactionID());
        if (idScore > 0) {
            componentScores.put(HEADER_TRANSACTION_ID, idScore);
        }

        final double amountResult = scoreAmount(transaction1.getTransactionAmount(), transaction2.getTransactionAmount());
        if (amountResult > 0) {
            componentScores.put(HEADER_TRANSACTION_AMOUNT, amountResult);
        }

        final double dateResult = scoreDate(transaction1.getTransactionDate(), transaction2.getTransactionDate());
        if (dateResult > 0) {
            componentScores.put(HEADER_TRANSACTION_DATE, dateResult);
        }

        final double walletScore = scoreWalletReference(transaction1.getWalletReference(), transaction2.getWalletReference());
        if (walletScore > 0) {
            componentScores.put(HEADER_WALLET_REFERENCE, walletScore);
        }

        final double narrativeResult = scoreStringSimilarity(
                transaction1.getTransactionNarrative(),
                transaction2.getTransactionNarrative(),
                ScoringWeights.NARRATIVE_SIMILARITY_WEIGHT
        );

        if (narrativeResult > 0) {
            componentScores.put(HEADER_TRANSACTION_NARRATIVE, narrativeResult);
        }

        final double descriptionResult = scoreStringSimilarity(
                transaction1.getTransactionDescription(),
                transaction2.getTransactionDescription(),
                ScoringWeights.DESCRIPTION_SIMILARITY_WEIGHT
        );

        if (descriptionResult > 0) {
            componentScores.put(HEADER_TRANSACTION_DESCRIPTION, descriptionResult);
        }

        final double typeScore = scoreTransactionType(transaction1.getTransactionType(), transaction2.getTransactionType());
        if (typeScore > 0) {
            componentScores.put(HEADER_TRANSACTION_TYPE, typeScore);
        }

        final double profileScore = scoreProfileName(transaction1.getProfileName(), transaction2.getProfileName());
        if (profileScore > 0) {
            componentScores.put(HEADER_PROFILE_NAME, profileScore);
        }

        final double totalScore = componentScores.values().stream().mapToDouble(v -> v).sum();

        final MatchConfidence confidence = determineConfidence(totalScore, componentScores);

        return new MatchScore(totalScore, confidence, componentScores);
    }

    private double scoreTransactionId(TransactionId id1, TransactionId id2) {
        if (id1 == null || id2 == null) {
            return 0;
        }

        if (Objects.equals(id1, id2)) {
            return ScoringWeights.TRANSACTION_ID_WEIGHT;
        }

        return 0;
    }

    private double scoreAmount(Double amount1, Double amount2) {
        if (amount1 == null || amount2 == null) {
            return 0;
        }

        // Exact match
        if (Objects.equals(amount1, amount2)) {
            return ScoringWeights.AMOUNT_EXACT_WEIGHT;
        }

        // Tolerance match
        final double difference = Math.abs(amount1 - amount2);
        final double tolerance = Math.max(amount1, amount2) * AMOUNT_TOLERANCE_PERCENTAGE;

        if (difference <= tolerance) {
            return ScoringWeights.AMOUNT_TOLERANCE_WEIGHT * (1.0 - (difference / tolerance));
        }

        return 0;
    }

    private double scoreDate(ZonedDateTime date1, ZonedDateTime date2) {
        if (date1 == null || date2 == null) {
            return 0;
        }

        final LocalDate localDate1 = date1.toLocalDate();
        final LocalDate localDate2 = date2.toLocalDate();

        // Exact date match
        if (localDate1.equals(localDate2)) {
            return ScoringWeights.DATE_EXACT_WEIGHT;
        }

        // Date tolerance match
        final long daysDifference = Math.abs(ChronoUnit.DAYS.between(localDate1, localDate2));

        if (daysDifference <= DATE_TOLERANCE_DAYS) {
            return ScoringWeights.DATE_TOLERANCE_WEIGHT *
                    (1.0 - (double) daysDifference / DATE_TOLERANCE_DAYS);
        }

        return 0;
    }

    private double scoreWalletReference(String wallet1, String wallet2) {
        if (wallet1 == null || wallet2 == null || wallet1.trim().isEmpty() || wallet2.trim().isEmpty()) {
            return 0;
        }

        if (wallet1.trim().equalsIgnoreCase(wallet2.trim())) {
            return ScoringWeights.WALLET_REFERENCE_WEIGHT;
        }

        return 0;
    }

    private double scoreStringSimilarity(String str1, String str2, double maxWeight) {
        if (str1 == null || str2 == null || str1.trim().isEmpty() || str2.trim().isEmpty()) {
            return 0;
        }

        final String normalized1 = str1.toLowerCase().trim();
        final String normalized2 = str2.toLowerCase().trim();

        // Exact match
        if (normalized1.equals(normalized2)) {
            return maxWeight;
        }

        // Similarity match
        final double similarity = calculateStringJaroWinklerSimilarity(normalized1, normalized2);

        if (similarity >= STRING_SIMILARITY_THRESHOLD) {
            return maxWeight * similarity;
        }

        return 0;
    }

    private double scoreTransactionType(TransactionType type1, TransactionType type2) {
        if (type1 == null || type2 == null) {
            return 0;
        }

        if (type1.equals(type2)) {
            return ScoringWeights.TRANSACTION_TYPE_WEIGHT;
        }

        return 0;
    }

    private double scoreProfileName(String profile1, String profile2) {
        if (profile1 == null || profile2 == null || profile1.trim().isEmpty() || profile2.trim().isEmpty()) {
            return 0;
        }

        if (profile1.trim().equalsIgnoreCase(profile2.trim())) {
            return 1.0;
        }

        return 0;
    }

    // Did not know about this library, found out when I needed it
    private double calculateStringJaroWinklerSimilarity(String s1, String s2) {
        if (s1 == null || s2 == null) return 0;
        if (s1.isEmpty() && s2.isEmpty()) return 1.0;

        return jaroWinkler.apply(s1.toLowerCase().trim(), s2.toLowerCase().trim());
    }

    private MatchConfidence determineConfidence(double totalScore, Map<String, Double> componentScores) {
        // High confidence: 80+ points AND/OR strong ID match
        if (totalScore >= 80.0) {
            return MatchConfidence.HIGH;
        }

        // Transaction ID match is high confidence if it contributes significantly
        if (componentScores.containsKey(HEADER_TRANSACTION_ID) && totalScore >= 70.0) {
            return MatchConfidence.HIGH;
        }

        // Medium confidence: 50-79 points with critical fields
        if (totalScore >= 50.0 && componentScores.containsKey(HEADER_TRANSACTION_AMOUNT) && componentScores.containsKey(HEADER_TRANSACTION_DATE)) {
            return MatchConfidence.MEDIUM;
        }

        // Low confidence: 20-49 points
        if (totalScore >= 20.0) {
            return MatchConfidence.LOW;
        }

        return MatchConfidence.VERY_LOW;
    }

}