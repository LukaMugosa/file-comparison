package luka.mugosa.filecomparison.service.score;

import luka.mugosa.filecomparison.domain.dto.TransactionDto;
import luka.mugosa.filecomparison.domain.enumeration.TransactionType;
import luka.mugosa.filecomparison.domain.score.dto.MatchConfidence;
import luka.mugosa.filecomparison.domain.score.dto.MatchScore;
import luka.mugosa.filecomparison.domain.score.dto.ScoringWeights;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.util.Map;

import static luka.mugosa.filecomparison.service.util.TransactionUtil.createPerfectMatchTransaction;
import static luka.mugosa.filecomparison.service.util.TransactionUtil.createTransaction;
import static luka.mugosa.filecomparison.service.util.TransactionUtil.createTransactionWithNarrative;
import static luka.mugosa.filecomparison.service.util.TransactionUtil.createTransactionWithType;
import static luka.mugosa.filecomparison.service.util.TransactionUtil.createTransactionWithWallet;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ScoreServiceImplTest {

    private ScoreServiceImpl scoreService;

    @BeforeEach
    void setUp() {
        scoreService = new ScoreServiceImpl();
    }

    @Nested
    @DisplayName("Transaction ID Scoring Tests")
    class TransactionIdScoringTests {

        @Test
        @DisplayName("Should return full score for exact transaction ID match")
        void shouldReturnFullScoreForExactIdMatch() {
            final ZonedDateTime date = ZonedDateTime.now();
            final TransactionDto txn1 = createTransaction("TXN001", 100.0, date);
            final TransactionDto txn2 = createTransaction("TXN001", 100.0, date);

            final MatchScore score = scoreService.calculateScore(txn1, txn2);

            assertNotNull(score);
            assertEquals(MatchConfidence.HIGH, score.getConfidence());
            assertTrue(score.getComponentScores().containsKey("TransactionID"));
            assertEquals(ScoringWeights.TRANSACTION_ID_WEIGHT, score.getComponentScores().get("TransactionID"));
            assertEquals(81.0, score.getTotalScore());
        }

        @Test
        @DisplayName("Should return very low confidence for different transaction IDs")
        void shouldReturnVeryLowConfidenceForDifferentIds() {
            final TransactionDto txn1 = createTransaction("TXN001", 100.0, ZonedDateTime.now());
            final TransactionDto txn2 = createTransaction("TXN002", 100.0, ZonedDateTime.now());

            final MatchScore score = scoreService.calculateScore(txn1, txn2);

            assertNotNull(score);
            final Map<String, Double> componentScores = score.getComponentScores();
            assertFalse(componentScores.containsKey("TransactionID"));
            assertEquals(41.0, score.getTotalScore());
            assertEquals(MatchConfidence.LOW, score.getConfidence());
            assertEquals(25.0, componentScores.get("TransactionAmount"));
            assertEquals(1.0, componentScores.get("ProfileName"));
            assertEquals(15.0, componentScores.get("TransactionDate"));
        }

        @Test
        @DisplayName("Should return zero score when transaction ID is null")
        void shouldReturnVeryLowConfidenceWhenIdIsNull() {
            final TransactionDto txn1 = createTransaction(null, 100.0, ZonedDateTime.now());
            final TransactionDto txn2 = createTransaction("TXN001", 100.0, ZonedDateTime.now());

            final MatchScore score = scoreService.calculateScore(txn1, txn2);

            assertNotNull(score);
            final Map<String, Double> componentScores = score.getComponentScores();
            assertFalse(componentScores.containsKey("TransactionID"));
            assertEquals(41.0, score.getTotalScore());

            assertEquals(MatchConfidence.LOW, score.getConfidence());

            assertEquals(25.0, componentScores.get("TransactionAmount"));
            assertEquals(1.0, componentScores.get("ProfileName"));
            assertEquals(15.0, componentScores.get("TransactionDate"));
        }
    }

    @Nested
    @DisplayName("Amount Scoring Tests")
    class AmountScoringTests {

        @Test
        @DisplayName("Should return tolerance score for amount within 1% tolerance")
        void shouldReturnToleranceScoreForAmountWithinTolerance() {
            final ZonedDateTime date = ZonedDateTime.now();
            final TransactionDto txn1 = createTransaction("TXN001", 100.0, date);
            final TransactionDto txn2 = createTransaction("TXN001", 100.5, date);

            final MatchScore score = scoreService.calculateScore(txn1, txn2);

            assertNotNull(score);
            assertEquals(MatchConfidence.HIGH, score.getConfidence());
            assertTrue(score.getComponentScores().containsKey("TransactionAmount"));
            final double amountScore = score.getComponentScores().get("TransactionAmount");
            assertTrue(amountScore > 0 && amountScore < ScoringWeights.AMOUNT_EXACT_WEIGHT);
            assertTrue(score.getTotalScore() > 0);
        }

        @Test
        @DisplayName("Should return low confidence for amount beyond tolerance")
        void shouldReturnLowConfidenceForAmountBeyondTolerance() {
            final ZonedDateTime date = ZonedDateTime.now();
            final TransactionDto txn1 = createTransaction("TXN001", 100.0, date);
            final TransactionDto txn2 = createTransaction("TXN001", 105.0, date);

            final MatchScore score = scoreService.calculateScore(txn1, txn2);

            assertNotNull(score);
            assertEquals(MatchConfidence.LOW, score.getConfidence()); // ID + Date still match
            assertFalse(score.getComponentScores().containsKey("TransactionAmount"));
            assertTrue(score.getTotalScore() > 0); // Still has ID and Date scores
        }

        @Test
        @DisplayName("Should return zero score when amount is null")
        void shouldReturnZeroScoreWhenAmountIsNull() {
            final ZonedDateTime date = ZonedDateTime.now();
            final TransactionDto txn1 = createTransaction("TXN001", null, date);
            final TransactionDto txn2 = createTransaction("TXN001", 100.0, date);

            final MatchScore score = scoreService.calculateScore(txn1, txn2);

            assertNotNull(score);
            assertEquals(MatchConfidence.LOW, score.getConfidence()); // ID + Date still match
            assertFalse(score.getComponentScores().containsKey("TransactionAmount"));
            assertTrue(score.getTotalScore() > 0); // Still has ID and Date scores
        }

        @Test
        @DisplayName("Should handle zero amounts correctly")
        void shouldHandleZeroAmountsCorrectly() {
            final ZonedDateTime date = ZonedDateTime.now();
            final TransactionDto txn1 = createTransaction("TXN001", 0.0, date);
            final TransactionDto txn2 = createTransaction("TXN001", 0.0, date);

            final MatchScore score = scoreService.calculateScore(txn1, txn2);

            assertNotNull(score);
            assertEquals(MatchConfidence.HIGH, score.getConfidence());
            assertTrue(score.getComponentScores().containsKey("TransactionAmount"));
            assertEquals(ScoringWeights.AMOUNT_EXACT_WEIGHT, score.getComponentScores().get("TransactionAmount"));
            assertTrue(score.getTotalScore() >= 80.0);
        }
    }

    @Nested
    @DisplayName("Date Scoring Tests")
    class DateScoringTests {

        @Test
        @DisplayName("Should return tolerance score for date within 2 days")
        void shouldReturnToleranceScoreForDateWithinTolerance() {
            final ZonedDateTime date1 = ZonedDateTime.now();
            final ZonedDateTime date2 = date1.plusDays(1);
            final TransactionDto txn1 = createTransaction("TXN001", 100.0, date1);
            final TransactionDto txn2 = createTransaction("TXN001", 100.0, date2);

            final MatchScore score = scoreService.calculateScore(txn1, txn2);

            assertTrue(score.getComponentScores().containsKey("TransactionDate"));
            final double dateScore = score.getComponentScores().get("TransactionDate");
            assertTrue(dateScore > 0 && dateScore < ScoringWeights.DATE_EXACT_WEIGHT);
        }

        @Test
        @DisplayName("Should return high score for date beyond tolerance but matching other parameters")
        void transactionDateNotPresentInComponents() {
            final ZonedDateTime date1 = ZonedDateTime.now();
            final ZonedDateTime date2 = date1.plusDays(5);
            final TransactionDto txn1 = createTransaction("TXN001", 100.0, date1);
            final TransactionDto txn2 = createTransaction("TXN001", 100.0, date2);

            final MatchScore score = scoreService.calculateScore(txn1, txn2);

            assertFalse(score.getComponentScores().containsKey("TransactionDate"));
            assertEquals(66.0, score.getTotalScore());
            assertEquals(MatchConfidence.HIGH, score.getConfidence());
        }

        @Test
        @DisplayName("Should return high score when date is null, but matching other parameters")
        void shouldReturnHighScoreWhenDateIsNullButMatchingOtherParameters() {
            final TransactionDto txn1 = createTransaction("TXN001", 100.0, null);
            final TransactionDto txn2 = createTransaction("TXN001", 100.0, ZonedDateTime.now());

            final MatchScore score = scoreService.calculateScore(txn1, txn2);

            assertFalse(score.getComponentScores().containsKey("TransactionDate"));
            assertEquals(66.0, score.getTotalScore());
            assertEquals(MatchConfidence.HIGH, score.getConfidence());
        }

        @Test
        @DisplayName("Should handle same date at different times correctly")
        void shouldHandleSameDateDifferentTimesCorrectly() {
            final ZonedDateTime date1 = ZonedDateTime.now().withHour(10).withMinute(0);
            final ZonedDateTime date2 = ZonedDateTime.now().withHour(23).withMinute(59);
            final TransactionDto txn1 = createTransaction("TXN001", 100.0, date1);
            final TransactionDto txn2 = createTransaction("TXN001", 100.0, date2);

            final MatchScore score = scoreService.calculateScore(txn1, txn2);

            assertTrue(score.getComponentScores().containsKey("TransactionDate"));
            assertEquals(ScoringWeights.DATE_EXACT_WEIGHT, score.getComponentScores().get("TransactionDate"));
            assertEquals(81.0, score.getTotalScore());
            assertEquals(MatchConfidence.HIGH, score.getConfidence());
        }
    }

    @Nested
    @DisplayName("String Similarity Tests")
    class StringSimilarityTests {

        @Test
        @DisplayName("Should return full score for exact narrative match")
        void shouldReturnFullScoreForExactNarrativeMatch() {
            final TransactionDto txn1 = createTransactionWithNarrative("TXN001", "PAYMENT TO STORE");
            final TransactionDto txn2 = createTransactionWithNarrative("TXN001", "PAYMENT TO STORE");

            final MatchScore score = scoreService.calculateScore(txn1, txn2);

            assertTrue(score.getComponentScores().containsKey("TransactionNarrative"));
            assertEquals(ScoringWeights.NARRATIVE_SIMILARITY_WEIGHT,
                    score.getComponentScores().get("TransactionNarrative"));
            assertEquals(86.0, score.getTotalScore());
            assertEquals(MatchConfidence.HIGH, score.getConfidence());
        }

        @Test
        @DisplayName("Should be case insensitive for narrative matching")
        void shouldBeCaseInsensitiveForNarrativeMatching() {
            final TransactionDto txn1 = createTransactionWithNarrative("TXN001", "payment to store");
            final TransactionDto txn2 = createTransactionWithNarrative("TXN001", "PAYMENT TO STORE");

            final MatchScore score = scoreService.calculateScore(txn1, txn2);

            assertTrue(score.getComponentScores().containsKey("TransactionNarrative"));
            assertEquals(ScoringWeights.NARRATIVE_SIMILARITY_WEIGHT,
                    score.getComponentScores().get("TransactionNarrative"));
            assertEquals(86.0, score.getTotalScore());
            assertEquals(MatchConfidence.HIGH, score.getConfidence());
        }

        @Test
        @DisplayName("Should score similar narratives using Jaro-Winkler")
        void shouldScoreSimilarNarrativesUsingJaroWinkler() {
            final TransactionDto txn1 = createTransactionWithNarrative("TXN001", "PAYMENT TO STORE ABC");
            final TransactionDto txn2 = createTransactionWithNarrative("TXN001", "PAYMENT TO STORE XYZ");

            final MatchScore score = scoreService.calculateScore(txn1, txn2);

            assertTrue(score.getComponentScores().containsKey("TransactionNarrative"));
            final double narrativeScore = score.getComponentScores().get("TransactionNarrative");
            assertTrue(narrativeScore > 0 && narrativeScore < ScoringWeights.NARRATIVE_SIMILARITY_WEIGHT);
            assertEquals(85.7, score.getTotalScore());
            assertEquals(MatchConfidence.HIGH, score.getConfidence());
        }

        @Test
        @DisplayName("Should return zero for completely different narratives")
        void shouldReturnZeroForCompletelyDifferentNarratives() {
            final TransactionDto txn1 = createTransactionWithNarrative("TXN001", "PAYMENT");
            final TransactionDto txn2 = createTransactionWithNarrative("TXN001", "XXXXXXXX");

            final MatchScore score = scoreService.calculateScore(txn1, txn2);

            assertFalse(score.getComponentScores().containsKey("TransactionNarrative"));
            assertEquals(81.0, score.getTotalScore());
            assertEquals(MatchConfidence.HIGH, score.getConfidence());
        }

        @Test
        @DisplayName("Transaction narrative not present in componentScores")
        void transactionNarrativeNotPresentInComponentsBecauseIsNull() {
            final TransactionDto txn1 = createTransactionWithNarrative("TXN001", null);
            final TransactionDto txn2 = createTransactionWithNarrative("TXN001", "PAYMENT");

            final MatchScore score = scoreService.calculateScore(txn1, txn2);

            assertFalse(score.getComponentScores().containsKey("TransactionNarrative"));
            assertEquals(81.0, score.getTotalScore());
            assertEquals(MatchConfidence.HIGH, score.getConfidence());
        }

        @Test
        @DisplayName("Should handle whitespace in narrative matching")
        void shouldHandleWhitespaceInNarrativeMatching() {
            final TransactionDto txn1 = createTransactionWithNarrative("TXN001", "  PAYMENT  ");
            final TransactionDto txn2 = createTransactionWithNarrative("TXN001", "PAYMENT");

            final MatchScore score = scoreService.calculateScore(txn1, txn2);

            assertTrue(score.getComponentScores().containsKey("TransactionNarrative"));
            assertEquals(ScoringWeights.NARRATIVE_SIMILARITY_WEIGHT,
                    score.getComponentScores().get("TransactionNarrative"));
            assertEquals(86.0, score.getTotalScore());
            assertEquals(MatchConfidence.HIGH, score.getConfidence());
        }
    }

    @Nested
    @DisplayName("Wallet Reference Scoring Tests")
    class WalletReferenceScoringTests {

        @Test
        @DisplayName("Should return full score for exact wallet reference match")
        void shouldReturnFullScoreForExactWalletReferenceMatch() {
            final TransactionDto txn1 = createTransactionWithWallet("TXN001", "WALLET123");
            final TransactionDto txn2 = createTransactionWithWallet("TXN001", "WALLET123");

            final MatchScore score = scoreService.calculateScore(txn1, txn2);

            assertTrue(score.getComponentScores().containsKey("WalletReference"));
            assertEquals(ScoringWeights.WALLET_REFERENCE_WEIGHT,
                    score.getComponentScores().get("WalletReference"));
            assertEquals(91, score.getTotalScore());
            assertEquals(MatchConfidence.HIGH, score.getConfidence());
        }

        @Test
        @DisplayName("Should be case insensitive for wallet reference")
        void shouldBeCaseInsensitiveForWalletReference() {
            final TransactionDto txn1 = createTransactionWithWallet("TXN001", "wallet123");
            final TransactionDto txn2 = createTransactionWithWallet("TXN001", "WALLET123");

            final MatchScore score = scoreService.calculateScore(txn1, txn2);

            assertTrue(score.getComponentScores().containsKey("WalletReference"));
            assertEquals(ScoringWeights.WALLET_REFERENCE_WEIGHT,
                    score.getComponentScores().get("WalletReference"));
            assertEquals(91, score.getTotalScore());
            assertEquals(MatchConfidence.HIGH, score.getConfidence());
        }

        @Test
        @DisplayName("Should return zero for different wallet references")
        void shouldReturnZeroForDifferentWalletReferences() {
            final TransactionDto txn1 = createTransactionWithWallet("TXN001", "WALLET123");
            final TransactionDto txn2 = createTransactionWithWallet("TXN001", "WALLET456");

            final MatchScore score = scoreService.calculateScore(txn1, txn2);

            assertFalse(score.getComponentScores().containsKey("WalletReference"));
            assertEquals(81, score.getTotalScore());
            assertEquals(MatchConfidence.HIGH, score.getConfidence());
        }

        @Test
        @DisplayName("Should return zero when wallet reference is null")
        void shouldReturnZeroWhenWalletReferenceIsNull() {
            final TransactionDto txn1 = createTransactionWithWallet("TXN001", null);
            final TransactionDto txn2 = createTransactionWithWallet("TXN001", "WALLET123");

            final MatchScore score = scoreService.calculateScore(txn1, txn2);

            assertFalse(score.getComponentScores().containsKey("WalletReference"));
            assertEquals(81, score.getTotalScore());
            assertEquals(MatchConfidence.HIGH, score.getConfidence());
        }
    }

    @Nested
    @DisplayName("Transaction Type Scoring Tests")
    class TransactionTypeScoringTests {

        @Test
        @DisplayName("Should return full score for matching transaction types")
        void shouldReturnFullScoreForMatchingTransactionTypes() {
            final TransactionDto txn1 = createTransactionWithType("TXN001", TransactionType.TYPE_1);
            final TransactionDto txn2 = createTransactionWithType("TXN001", TransactionType.TYPE_1);

            final MatchScore score = scoreService.calculateScore(txn1, txn2);

            assertTrue(score.getComponentScores().containsKey("TransactionType"));
            assertEquals(ScoringWeights.TRANSACTION_TYPE_WEIGHT,
                    score.getComponentScores().get("TransactionType"));
            assertEquals(83, score.getTotalScore());
            assertEquals(MatchConfidence.HIGH, score.getConfidence());
        }

        @Test
        @DisplayName("Should return zero for different transaction types")
        void shouldReturnZeroForDifferentTransactionTypes() {
            final TransactionDto txn1 = createTransactionWithType("TXN001", TransactionType.TYPE_1);
            final TransactionDto txn2 = createTransactionWithType("TXN001", TransactionType.TYPE_2);

            final MatchScore score = scoreService.calculateScore(txn1, txn2);

            assertFalse(score.getComponentScores().containsKey("TransactionType"));
            assertEquals(81, score.getTotalScore());
            assertEquals(MatchConfidence.HIGH, score.getConfidence());
        }

        @Test
        @DisplayName("Should return zero when transaction type is null")
        void shouldReturnZeroWhenTransactionTypeIsNull() {
            final TransactionDto txn1 = createTransactionWithType("TXN001", null);
            final TransactionDto txn2 = createTransactionWithType("TXN001", TransactionType.TYPE_1);

            final MatchScore score = scoreService.calculateScore(txn1, txn2);

            assertFalse(score.getComponentScores().containsKey("TransactionType"));
            assertEquals(81, score.getTotalScore());
            assertEquals(MatchConfidence.HIGH, score.getConfidence());
        }
    }

    @Nested
    @DisplayName("Match Confidence Tests")
    class MatchConfidenceTests {

        @Test
        @DisplayName("Should return HIGH confidence for score >= 80")
        void shouldReturnHighConfidenceForScoreAbove80() {
            final TransactionDto txn1 = createPerfectMatchTransaction();
            final TransactionDto txn2 = createPerfectMatchTransaction();

            final MatchScore score = scoreService.calculateScore(txn1, txn2);

            assertEquals(MatchConfidence.HIGH, score.getConfidence());
            assertEquals(101.0, score.getTotalScore());
        }

        @Test
        @DisplayName("Should return LOW confidence without transaction ids")
        void shouldReturnMediumConfidenceForScore50to79WithAmountAndDate() {
            final ZonedDateTime date = ZonedDateTime.now();
            // Create transactions without ID but with amount and date
            final TransactionDto txn1 = createTransaction(null, 100.0, date);
            final TransactionDto txn2 = createTransaction(null, 100.0, date);

            final MatchScore score = scoreService.calculateScore(txn1, txn2);

            assertEquals(41, score.getTotalScore());
            assertEquals(MatchConfidence.LOW, score.getConfidence());
            assertTrue(score.getComponentScores().containsKey("TransactionAmount"));
            assertTrue(score.getComponentScores().containsKey("TransactionDate"));
        }

        @Test
        @DisplayName("Should return VERY_LOW confidence for score < 20")
        void shouldReturnVeryLowConfidenceForScoreBelow20() {
            final TransactionDto txn1 = createTransaction("TXN001", 100.0, ZonedDateTime.now());
            final TransactionDto txn2 = createTransaction("TXN002", 200.0, ZonedDateTime.now().plusDays(10));

            final MatchScore score = scoreService.calculateScore(txn1, txn2);

            assertTrue(score.getTotalScore() < 20.0);
            assertEquals(MatchConfidence.VERY_LOW, score.getConfidence());
        }
    }

    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {

        @Test
        @DisplayName("Should handle completely mismatched transactions")
        void shouldHandleCompletelyMismatchedTransactions() {
            final TransactionDto txn1 = createTransaction("TXN001", 100.0, ZonedDateTime.now(), "ProfileName1");
            final TransactionDto txn2 = createTransaction("TXN999", 999.0, ZonedDateTime.now().plusYears(1), "ProfileName2");

            final MatchScore score = scoreService.calculateScore(txn1, txn2);

            assertEquals(MatchConfidence.VERY_LOW, score.getConfidence());
            assertEquals(0.0, score.getTotalScore());
        }

        @Test
        @DisplayName("Should handle all null fields gracefully")
        void shouldHandleAllNullFieldsGracefully() {
            final TransactionDto txn1 = createTransaction(null, null, null, null);
            final TransactionDto txn2 = createTransaction(null, null, null, null);

            final MatchScore score = scoreService.calculateScore(txn1, txn2);

            assertEquals(0.0, score.getTotalScore());
            assertEquals(MatchConfidence.VERY_LOW, score.getConfidence());
        }

        @Test
        @DisplayName("Should calculate component scores correctly")
        void shouldCalculateComponentScoresCorrectly() {
            final ZonedDateTime date = ZonedDateTime.now();
            final TransactionDto txn1 = createTransaction("TXN001", 100.0, date);
            final TransactionDto txn2 = createTransaction("TXN001", 100.0, date);

            final MatchScore score = scoreService.calculateScore(txn1, txn2);

            final Map<String, Double> components = score.getComponentScores();
            assertNotNull(components);
            assertTrue(components.size() > 0);

            final double manualSum = components.values().stream().mapToDouble(v -> v).sum();
            assertEquals(score.getTotalScore(), manualSum, 0.001);
        }
    }
}