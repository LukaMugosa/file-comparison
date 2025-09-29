package luka.mugosa.filecomparison.service.impl;

import luka.mugosa.filecomparison.domain.dto.TransactionDto;
import luka.mugosa.filecomparison.domain.dto.UnmatchedTransactionPairDto;
import luka.mugosa.filecomparison.domain.dto.response.ReconciliationResponse;
import luka.mugosa.filecomparison.domain.enumeration.TransactionType;
import luka.mugosa.filecomparison.domain.id.TransactionId;
import luka.mugosa.filecomparison.domain.score.dto.MatchConfidence;
import luka.mugosa.filecomparison.domain.score.dto.MatchScore;
import luka.mugosa.filecomparison.service.ScoreService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static luka.mugosa.filecomparison.service.util.TransactionUtil.createMatchScore;
import static luka.mugosa.filecomparison.service.util.TransactionUtil.createTransactionList;
import static luka.mugosa.filecomparison.service.util.TransactionUtil.createTransactionTwoMainParams;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ComparisonServiceImplTest {

    @Mock
    private ScoreService scoreService;

    private ComparisonServiceImpl comparisonService;

    @BeforeEach
    void setUp() {
        comparisonService = new ComparisonServiceImpl(scoreService);
    }

    @Nested
    @DisplayName("Perfect Match Scenarios")
    class PerfectMatchScenarios {

        @Test
        @DisplayName("Should match all transactions when all have HIGH confidence")
        void shouldMatchAllTransactionsWhenAllHighConfidence() {
            // Arrange
            final List<TransactionDto> collection1 = createTransactionList(
                    createTransactionTwoMainParams("TXN001", 100.0),
                    createTransactionTwoMainParams("TXN002", 200.0),
                    createTransactionTwoMainParams("TXN003", 300.0)
            );

            final List<TransactionDto> collection2 = createTransactionList(
                    createTransactionTwoMainParams("TXN001", 100.0),
                    createTransactionTwoMainParams("TXN002", 200.0),
                    createTransactionTwoMainParams("TXN003", 300.0)
            );

            when(scoreService.calculateScore(any(), any()))
                    .thenReturn(createMatchScore(95.0, MatchConfidence.HIGH));

            // Act
            final ReconciliationResponse response = comparisonService.compareData(collection1, collection2);

            // Assert - Response metadata
            assertNotNull(response);
            assertEquals(3, response.getTotalRecordsInFile1());
            assertEquals(3, response.getTotalRecordsInFile2());
            assertEquals(3, response.getMatchedRecords());
            assertEquals(0, response.getUnmatchedRecordsInFile1());
            assertEquals(0, response.getUnmatchedRecordsInFile2());
            assertEquals(100.0, response.getMatchPercentage());

            // Assert - Unmatched pairs
            assertNotNull(response.getUnmatchedTransactionPairs());
            assertTrue(response.getUnmatchedTransactionPairs().isEmpty());

            // Verify interactions
            verify(scoreService, times(3)).calculateScore(any(), any());
        }

        @Test
        @DisplayName("Should match single transaction with HIGH confidence")
        void shouldMatchSingleTransactionWithHighConfidence() {
            // Arrange
            final TransactionDto txn1 = createTransactionTwoMainParams("TXN001", 100.0);
            final TransactionDto txn2 = createTransactionTwoMainParams("TXN001", 100.0);

            final List<TransactionDto> collection1 = createTransactionList(txn1);
            final List<TransactionDto> collection2 = createTransactionList(txn2);

            when(scoreService.calculateScore(txn1, txn2))
                    .thenReturn(createMatchScore(90.0, MatchConfidence.HIGH));

            // Act
            final ReconciliationResponse response = comparisonService.compareData(collection1, collection2);

            // Assert
            assertNotNull(response);
            assertEquals(1, response.getTotalRecordsInFile1());
            assertEquals(1, response.getTotalRecordsInFile2());
            assertEquals(1, response.getMatchedRecords());
            assertEquals(0, response.getUnmatchedRecordsInFile1());
            assertEquals(0, response.getUnmatchedRecordsInFile2());
            assertEquals(100.0, response.getMatchPercentage());
            assertTrue(response.getUnmatchedTransactionPairs().isEmpty());

            verify(scoreService, times(1)).calculateScore(txn1, txn2);
        }
    }

    @Nested
    @DisplayName("Low Confidence Match Scenarios")
    class LowConfidenceMatchScenarios {

        @Test
        @DisplayName("Should treat MEDIUM confidence as unmatched")
        void shouldTreatMediumConfidenceAsUnmatched() {
            // Arrange
            final TransactionDto txn1 = createTransactionTwoMainParams("TXN001", 100.0);
            final TransactionDto txn2 = createTransactionTwoMainParams("TXN001", 105.0);

            final List<TransactionDto> collection1 = createTransactionList(txn1);
            final List<TransactionDto> collection2 = createTransactionList(txn2);

            when(scoreService.calculateScore(txn1, txn2))
                    .thenReturn(createMatchScore(55.0, MatchConfidence.MEDIUM));

            // Act
            final ReconciliationResponse response = comparisonService.compareData(collection1, collection2);

            // Assert - Match statistics
            assertNotNull(response);
            assertEquals(1, response.getTotalRecordsInFile1());
            assertEquals(1, response.getTotalRecordsInFile2());
            assertEquals(0, response.getMatchedRecords());
            assertEquals(1, response.getUnmatchedRecordsInFile1());
            assertEquals(0, response.getUnmatchedRecordsInFile2());
            assertEquals(0.0, response.getMatchPercentage());

            // Assert - Unmatched pairs
            assertNotNull(response.getUnmatchedTransactionPairs());
            assertEquals(1, response.getUnmatchedTransactionPairs().size());

            final UnmatchedTransactionPairDto pair = response.getUnmatchedTransactionPairs().get(0);
            assertNotNull(pair);
            assertNotNull(pair.getTransaction1());
            assertNotNull(pair.getTransaction2());
            assertEquals(txn1.getTransactionID(), pair.getTransaction1().getTransactionID());
            assertEquals(txn2.getTransactionID(), pair.getTransaction2().getTransactionID());

            verify(scoreService, times(1)).calculateScore(txn1, txn2);
        }

        @Test
        @DisplayName("Should treat LOW confidence as unmatched")
        void shouldTreatLowConfidenceAsUnmatched() {
            // Arrange
            final TransactionDto txn1 = createTransactionTwoMainParams("TXN001", 100.0);
            final TransactionDto txn2 = createTransactionTwoMainParams("TXN001", 150.0);

            final List<TransactionDto> collection1 = createTransactionList(txn1);
            final List<TransactionDto> collection2 = createTransactionList(txn2);

            when(scoreService.calculateScore(txn1, txn2))
                    .thenReturn(createMatchScore(35.0, MatchConfidence.LOW));

            // Act
            final ReconciliationResponse response = comparisonService.compareData(collection1, collection2);

            // Assert
            assertNotNull(response);
            assertEquals(0, response.getMatchedRecords());
            assertEquals(1, response.getUnmatchedRecordsInFile1());
            assertEquals(0, response.getUnmatchedRecordsInFile2());
            assertEquals(0.0, response.getMatchPercentage());

            assertEquals(1, response.getUnmatchedTransactionPairs().size());
            final UnmatchedTransactionPairDto pair = response.getUnmatchedTransactionPairs().get(0);
            assertNotNull(pair.getTransaction1());
            assertNotNull(pair.getTransaction2());
        }

        @Test
        @DisplayName("Should treat VERY_LOW confidence as unmatched")
        void shouldTreatVeryLowConfidenceAsUnmatched() {
            // Arrange
            final TransactionDto txn1 = createTransactionTwoMainParams("TXN001", 100.0);
            final TransactionDto txn2 = createTransactionTwoMainParams("TXN001", 999.0);

            final List<TransactionDto> collection1 = createTransactionList(txn1);
            final List<TransactionDto> collection2 = createTransactionList(txn2);

            when(scoreService.calculateScore(txn1, txn2))
                    .thenReturn(createMatchScore(5.0, MatchConfidence.VERY_LOW));

            // Act
            final ReconciliationResponse response = comparisonService.compareData(collection1, collection2);

            // Assert
            assertNotNull(response);
            assertEquals(0, response.getMatchedRecords());
            assertEquals(1, response.getUnmatchedRecordsInFile1());
            assertEquals(0.0, response.getMatchPercentage());
            assertEquals(1, response.getUnmatchedTransactionPairs().size());
        }
    }

    @Nested
    @DisplayName("Unmatched Transaction Scenarios")
    class UnmatchedTransactionScenarios {

        @Test
        @DisplayName("Should handle transaction only in file1")
        void shouldHandleTransactionOnlyInFile1() {
            // Arrange
            final TransactionDto txn1 = createTransactionTwoMainParams("TXN001", 100.0);
            final List<TransactionDto> collection1 = createTransactionList(txn1);
            final List<TransactionDto> collection2 = new ArrayList<>();

            // Act
            final ReconciliationResponse response = comparisonService.compareData(collection1, collection2);

            // Assert
            assertNotNull(response);
            assertEquals(1, response.getTotalRecordsInFile1());
            assertEquals(0, response.getTotalRecordsInFile2());
            assertEquals(0, response.getMatchedRecords());
            assertEquals(1, response.getUnmatchedRecordsInFile1());
            assertEquals(0, response.getUnmatchedRecordsInFile2());
            assertEquals(0.0, response.getMatchPercentage());

            // Assert unmatched pair
            assertEquals(1, response.getUnmatchedTransactionPairs().size());
            final UnmatchedTransactionPairDto pair = response.getUnmatchedTransactionPairs().get(0);
            assertNotNull(pair);
            assertNotNull(pair.getTransaction1());
            assertNull(pair.getTransaction2());
            assertEquals(txn1.getTransactionID(), pair.getTransaction1().getTransactionID());

            verifyNoInteractions(scoreService);
        }

        @Test
        @DisplayName("Should handle transaction only in file2")
        void shouldHandleTransactionOnlyInFile2() {
            // Arrange
            final TransactionDto txn2 = createTransactionTwoMainParams("TXN002", 200.0);
            final List<TransactionDto> collection1 = new ArrayList<>();
            final List<TransactionDto> collection2 = createTransactionList(txn2);

            // Act
            final ReconciliationResponse response = comparisonService.compareData(collection1, collection2);

            // Assert
            assertNotNull(response);
            assertEquals(0, response.getTotalRecordsInFile1());
            assertEquals(1, response.getTotalRecordsInFile2());
            assertEquals(0, response.getMatchedRecords());
            assertEquals(0, response.getUnmatchedRecordsInFile1());
            assertEquals(1, response.getUnmatchedRecordsInFile2());
            assertEquals(0.0, response.getMatchPercentage());

            // Assert unmatched pair
            assertEquals(1, response.getUnmatchedTransactionPairs().size());
            final UnmatchedTransactionPairDto pair = response.getUnmatchedTransactionPairs().get(0);
            assertNotNull(pair);
            assertNull(pair.getTransaction1());
            assertNotNull(pair.getTransaction2());
            assertEquals(txn2.getTransactionID(), pair.getTransaction2().getTransactionID());

            verifyNoInteractions(scoreService);
        }

        @Test
        @DisplayName("Should handle multiple unmatched transactions in both files")
        void shouldHandleMultipleUnmatchedTransactionsInBothFiles() {
            // Arrange
            final List<TransactionDto> collection1 = createTransactionList(
                    createTransactionTwoMainParams("TXN001", 100.0),
                    createTransactionTwoMainParams("TXN002", 200.0)
            );

            final List<TransactionDto> collection2 = createTransactionList(
                    createTransactionTwoMainParams("TXN003", 300.0),
                    createTransactionTwoMainParams("TXN004", 400.0)
            );

            // Act
            final ReconciliationResponse response = comparisonService.compareData(collection1, collection2);

            // Assert
            assertNotNull(response);
            assertEquals(2, response.getTotalRecordsInFile1());
            assertEquals(2, response.getTotalRecordsInFile2());
            assertEquals(0, response.getMatchedRecords());
            assertEquals(2, response.getUnmatchedRecordsInFile1());
            assertEquals(2, response.getUnmatchedRecordsInFile2());
            assertEquals(0.0, response.getMatchPercentage());

            // Assert unmatched pairs - should have 4 total (2 from file1, 2 from file2)
            assertEquals(4, response.getUnmatchedTransactionPairs().size());

            // Verify file1 unmatched pairs
            long file1OnlyPairs = response.getUnmatchedTransactionPairs().stream()
                    .filter(p -> p.getTransaction1() != null && p.getTransaction2() == null)
                    .count();
            assertEquals(2, file1OnlyPairs);

            // Verify file2 unmatched pairs
            long file2OnlyPairs = response.getUnmatchedTransactionPairs().stream()
                    .filter(p -> p.getTransaction1() == null && p.getTransaction2() != null)
                    .count();
            assertEquals(2, file2OnlyPairs);

            verifyNoInteractions(scoreService);
        }
    }

    @Nested
    @DisplayName("Mixed Scenarios")
    class MixedScenarios {

        @Test
        @DisplayName("Should handle mix of matched and unmatched transactions")
        void shouldHandleMixOfMatchedAndUnmatchedTransactions() {
            // Arrange
            final TransactionDto matched1File1 = createTransactionTwoMainParams("TXN001", 100.0);
            final TransactionDto matched1File2 = createTransactionTwoMainParams("TXN001", 100.0);
            final TransactionDto matched2File1 = createTransactionTwoMainParams("TXN002", 200.0);
            final TransactionDto matched2File2 = createTransactionTwoMainParams("TXN002", 200.0);
            final TransactionDto unmatchedFile1 = createTransactionTwoMainParams("TXN003", 300.0);
            final TransactionDto unmatchedFile2 = createTransactionTwoMainParams("TXN004", 400.0);

            final List<TransactionDto> collection1 = createTransactionList(
                    matched1File1, matched2File1, unmatchedFile1
            );

            final List<TransactionDto> collection2 = createTransactionList(
                    matched1File2, matched2File2, unmatchedFile2
            );

            when(scoreService.calculateScore(matched1File1, matched1File2))
                    .thenReturn(createMatchScore(90.0, MatchConfidence.HIGH));
            when(scoreService.calculateScore(matched2File1, matched2File2))
                    .thenReturn(createMatchScore(85.0, MatchConfidence.HIGH));

            // Act
            final ReconciliationResponse response = comparisonService.compareData(collection1, collection2);

            // Assert
            assertNotNull(response);
            assertEquals(3, response.getTotalRecordsInFile1());
            assertEquals(3, response.getTotalRecordsInFile2());
            assertEquals(2, response.getMatchedRecords());
            assertEquals(1, response.getUnmatchedRecordsInFile1());
            assertEquals(1, response.getUnmatchedRecordsInFile2());
            assertEquals(66.66666666666666, response.getMatchPercentage(), 0.0001);

            // Assert unmatched pairs
            assertEquals(2, response.getUnmatchedTransactionPairs().size());

            // Verify file1 unmatched
            final boolean hasFile1Unmatched = response.getUnmatchedTransactionPairs().stream()
                    .anyMatch(p -> p.getTransaction1() != null &&
                            p.getTransaction1().getTransactionID().equals(unmatchedFile1.getTransactionID()) &&
                            p.getTransaction2() == null);
            assertTrue(hasFile1Unmatched);

            // Verify file2 unmatched
            final boolean hasFile2Unmatched = response.getUnmatchedTransactionPairs().stream()
                    .anyMatch(p -> p.getTransaction2() != null &&
                            p.getTransaction2().getTransactionID().equals(unmatchedFile2.getTransactionID()) &&
                            p.getTransaction1() == null);
            assertTrue(hasFile2Unmatched);

            verify(scoreService, times(2)).calculateScore(any(), any());
        }

        @Test
        @DisplayName("Should handle same ID with low confidence as unmatched pair")
        void shouldHandleSameIdWithLowConfidenceAsUnmatchedPair() {
            // Arrange
            final TransactionDto highConfFile1 = createTransactionTwoMainParams("TXN001", 100.0);
            final TransactionDto highConfFile2 = createTransactionTwoMainParams("TXN001", 100.0);
            final TransactionDto lowConfFile1 = createTransactionTwoMainParams("TXN002", 200.0);
            final TransactionDto lowConfFile2 = createTransactionTwoMainParams("TXN002", 999.0);

            final List<TransactionDto> collection1 = createTransactionList(highConfFile1, lowConfFile1);
            final List<TransactionDto> collection2 = createTransactionList(highConfFile2, lowConfFile2);

            when(scoreService.calculateScore(highConfFile1, highConfFile2))
                    .thenReturn(createMatchScore(90.0, MatchConfidence.HIGH));
            when(scoreService.calculateScore(lowConfFile1, lowConfFile2))
                    .thenReturn(createMatchScore(10.0, MatchConfidence.VERY_LOW));

            // Act
            final ReconciliationResponse response = comparisonService.compareData(collection1, collection2);

            // Assert
            assertNotNull(response);
            assertEquals(2, response.getTotalRecordsInFile1());
            assertEquals(2, response.getTotalRecordsInFile2());
            assertEquals(1, response.getMatchedRecords());
            assertEquals(1, response.getUnmatchedRecordsInFile1());
            assertEquals(0, response.getUnmatchedRecordsInFile2());
            assertEquals(50.0, response.getMatchPercentage());

            // Assert unmatched pairs
            assertEquals(1, response.getUnmatchedTransactionPairs().size());
            final UnmatchedTransactionPairDto pair = response.getUnmatchedTransactionPairs().get(0);
            assertNotNull(pair);
            assertNotNull(pair.getTransaction1());
            assertNotNull(pair.getTransaction2());
            assertEquals(lowConfFile1.getTransactionID(), pair.getTransaction1().getTransactionID());
            assertEquals(lowConfFile2.getTransactionID(), pair.getTransaction2().getTransactionID());

            verify(scoreService, times(2)).calculateScore(any(), any());
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {

        @Test
        @DisplayName("Should handle empty collections")
        void shouldHandleEmptyCollections() {
            // Arrange
            final List<TransactionDto> collection1 = new ArrayList<>();
            final List<TransactionDto> collection2 = new ArrayList<>();

            // Act
            final ReconciliationResponse response = comparisonService.compareData(collection1, collection2);

            // Assert
            assertNotNull(response);
            assertEquals(0, response.getTotalRecordsInFile1());
            assertEquals(0, response.getTotalRecordsInFile2());
            assertEquals(0, response.getMatchedRecords());
            assertEquals(0, response.getUnmatchedRecordsInFile1());
            assertEquals(0, response.getUnmatchedRecordsInFile2());
            assertEquals(0.0, response.getMatchPercentage());
            assertTrue(response.getUnmatchedTransactionPairs().isEmpty());

            verifyNoInteractions(scoreService);
        }

        @Test
        @DisplayName("Should handle empty file1")
        void shouldHandleEmptyFile1() {
            // Arrange
            final List<TransactionDto> collection1 = new ArrayList<>();
            final List<TransactionDto> collection2 = createTransactionList(
                    createTransactionTwoMainParams("TXN001", 100.0)
            );

            // Act
            final ReconciliationResponse response = comparisonService.compareData(collection1, collection2);

            // Assert
            assertNotNull(response);
            assertEquals(0, response.getTotalRecordsInFile1());
            assertEquals(1, response.getTotalRecordsInFile2());
            assertEquals(0, response.getMatchedRecords());
            assertEquals(0, response.getUnmatchedRecordsInFile1());
            assertEquals(1, response.getUnmatchedRecordsInFile2());
            assertEquals(0.0, response.getMatchPercentage());
            assertEquals(1, response.getUnmatchedTransactionPairs().size());

            verifyNoInteractions(scoreService);
        }

        @Test
        @DisplayName("Should handle empty file2")
        void shouldHandleEmptyFile2() {
            // Arrange
            final List<TransactionDto> collection1 = createTransactionList(
                    createTransactionTwoMainParams("TXN001", 100.0)
            );
            final List<TransactionDto> collection2 = new ArrayList<>();

            // Act
            final ReconciliationResponse response = comparisonService.compareData(collection1, collection2);

            // Assert
            assertNotNull(response);
            assertEquals(1, response.getTotalRecordsInFile1());
            assertEquals(0, response.getTotalRecordsInFile2());
            assertEquals(0, response.getMatchedRecords());
            assertEquals(1, response.getUnmatchedRecordsInFile1());
            assertEquals(0, response.getUnmatchedRecordsInFile2());
            assertEquals(0.0, response.getMatchPercentage());
            assertEquals(1, response.getUnmatchedTransactionPairs().size());

            verifyNoInteractions(scoreService);
        }

        @Test
        @DisplayName("Should calculate correct percentage with partial matches")
        void shouldCalculateCorrectPercentageWithPartialMatches() {
            // Arrange - 3 out of 10 match
            final List<TransactionDto> collection1 = new ArrayList<>();
            final List<TransactionDto> collection2 = new ArrayList<>();

            for (int i = 1; i <= 10; i++) {
                final TransactionDto txn1 = createTransactionTwoMainParams("TXN00" + i, i * 100.0);
                final TransactionDto txn2 = createTransactionTwoMainParams("TXN00" + i, i * 100.0);
                collection1.add(txn1);
                collection2.add(txn2);

                if (i <= 3) {
                    when(scoreService.calculateScore(txn1, txn2))
                            .thenReturn(createMatchScore(90.0, MatchConfidence.HIGH));
                } else {
                    when(scoreService.calculateScore(txn1, txn2))
                            .thenReturn(createMatchScore(30.0, MatchConfidence.LOW));
                }
            }

            // Act
            final ReconciliationResponse response = comparisonService.compareData(collection1, collection2);

            // Assert
            assertNotNull(response);
            assertEquals(10, response.getTotalRecordsInFile1());
            assertEquals(10, response.getTotalRecordsInFile2());
            assertEquals(3, response.getMatchedRecords());
            assertEquals(7, response.getUnmatchedRecordsInFile1());
            assertEquals(0, response.getUnmatchedRecordsInFile2());
            assertEquals(30.0, response.getMatchPercentage());
            assertEquals(7, response.getUnmatchedTransactionPairs().size());

            verify(scoreService, times(10)).calculateScore(any(), any());
        }
    }

    @Nested
    @DisplayName("Large Dataset Tests")
    class LargeDatasetTests {

        @Test
        @DisplayName("Should handle large matching dataset efficiently")
        void shouldHandleLargeMatchingDatasetEfficiently() {
            // Arrange - 1000 transactions all matching
            final List<TransactionDto> collection1 = new ArrayList<>();
            final List<TransactionDto> collection2 = new ArrayList<>();

            for (int i = 1; i <= 1000; i++) {
                final TransactionDto txn = createTransactionTwoMainParams("TXN" + String.format("%04d", i), i * 100.0);
                collection1.add(txn);
                collection2.add(txn);
            }

            when(scoreService.calculateScore(any(), any()))
                    .thenReturn(createMatchScore(90.0, MatchConfidence.HIGH));

            // Act
            final ReconciliationResponse response = comparisonService.compareData(collection1, collection2);

            // Assert
            assertNotNull(response);
            assertEquals(1000, response.getTotalRecordsInFile1());
            assertEquals(1000, response.getTotalRecordsInFile2());
            assertEquals(1000, response.getMatchedRecords());
            assertEquals(0, response.getUnmatchedRecordsInFile1());
            assertEquals(0, response.getUnmatchedRecordsInFile2());
            assertEquals(100.0, response.getMatchPercentage());
            assertTrue(response.getUnmatchedTransactionPairs().isEmpty());

            verify(scoreService, times(1000)).calculateScore(any(), any());
        }
    }

}