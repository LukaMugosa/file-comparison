package luka.mugosa.filecomparison.service.impl;

import luka.mugosa.filecomparison.domain.dto.TransactionDto;
import luka.mugosa.filecomparison.domain.dto.UnmatchedTransactionPairDto;
import luka.mugosa.filecomparison.domain.dto.response.ReconciliationResponse;
import luka.mugosa.filecomparison.domain.id.TransactionId;
import luka.mugosa.filecomparison.domain.score.dto.MatchConfidence;
import luka.mugosa.filecomparison.domain.score.dto.MatchScore;
import luka.mugosa.filecomparison.service.ComparisonService;
import luka.mugosa.filecomparison.service.ScoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ComparisonServiceImpl implements ComparisonService {

    private static final Logger logger = LoggerFactory.getLogger(ComparisonServiceImpl.class);

    private final ScoreService scoreService;

    public ComparisonServiceImpl(ScoreService scoreService) {
        this.scoreService = scoreService;
    }

    public ReconciliationResponse compareData(Collection<TransactionDto> collection1, Collection<TransactionDto> collection2) {
        logger.info("Starting data comparison - Collection1: {} records, Collection2: {} records",
                collection1.size(), collection2.size());

        // Create maps for efficient lookups
        final Map<TransactionId, TransactionDto> transactionMapById1 = collection1.stream()
                .collect(Collectors.toMap(TransactionDto::getTransactionID, Function.identity()));
        final Map<TransactionId, TransactionDto> transactionMapById2 = collection2.stream()
                .collect(Collectors.toMap(TransactionDto::getTransactionID, Function.identity()));

        final int totalRecordsInFile1 = transactionMapById1.size();
        final int totalRecordsInFile2 = transactionMapById2.size();

        // Collections to track results
        int matchedRecords = 0;
        final List<TransactionDto> unmatchedFromFile1 = new ArrayList<>();
        final Set<TransactionId> processedFromFile2 = new HashSet<>();
        final List<UnmatchedTransactionPairDto> unmatchedTransactionPairs = new ArrayList<>();

        // Process transactions from file 1
        for (Map.Entry<TransactionId, TransactionDto> entry : transactionMapById1.entrySet()) {
            final TransactionId transactionId = entry.getKey();
            final TransactionDto dto1 = entry.getValue();

            if (transactionMapById2.containsKey(transactionId)) {
                final TransactionDto dto2 = transactionMapById2.get(transactionId);
                processedFromFile2.add(transactionId);

                final MatchScore matchScore = scoreService.calculateScore(dto1, dto2);

                // we can tune this by requirement, my opinion is that it should be like this
                if (matchScore.getConfidence() == MatchConfidence.HIGH) {
                    matchedRecords++;
                    logger.debug("Exact match found: ID={}, Score={}", transactionId, matchScore.getTotalScore());
                } else {
                    // Low confidence match - treat as an unmatched pair
                    unmatchedFromFile1.add(dto1);
                    unmatchedTransactionPairs.add(new UnmatchedTransactionPairDto(dto1, dto2));
                    logger.debug("Match rejected (low confidence): ID={}, Score={}, Confidence={}",
                            transactionId, matchScore.getTotalScore(), matchScore.getConfidence());
                }
            } else {
                // No matching ID found in file 2
                unmatchedFromFile1.add(dto1);
                unmatchedTransactionPairs.add(new UnmatchedTransactionPairDto(dto1, null));
            }
        }

        // Find unmatched transactions from file 2 (those not processed above)
        final List<TransactionDto> unmatchedFromFile2 = transactionMapById2.entrySet().stream()
                .filter(entry -> !processedFromFile2.contains(entry.getKey()))
                .map(Map.Entry::getValue)
                .toList();

        // Add file2-only unmatched transactions
        unmatchedTransactionPairs.addAll(
                unmatchedFromFile2.stream()
                        .map(t -> new UnmatchedTransactionPairDto(null, t))
                        .toList()
        );

        final int unmatchedRecordsInFile1 = unmatchedFromFile1.size();
        final int unmatchedRecordsInFile2 = unmatchedFromFile2.size();

        // Calculate match percentage
        final double matchPercentage = totalRecordsInFile1 > 0 ?
                (double) matchedRecords / totalRecordsInFile1 * 100 : 0.0;

        logger.info("=== Reconciliation Summary ===");
        logger.info("File1 total records: {}", totalRecordsInFile1);
        logger.info("File2 total records: {}", totalRecordsInFile2);
        logger.info("Matched records: {} ({}%)", matchedRecords, matchPercentage);
        logger.info("Total unmatched pairs: {}", unmatchedTransactionPairs.size());
        logger.info("==============================");

        if (matchPercentage < 50.0) {
            logger.warn("Low match rate detected: {}% - Review reconciliation criteria", matchPercentage);
        }

        return ReconciliationResponse.builder()
                .totalRecordsInFile1(totalRecordsInFile1)
                .totalRecordsInFile2(totalRecordsInFile2)
                .matchedRecords(matchedRecords)
                .unmatchedRecordsInFile1(unmatchedRecordsInFile1)
                .unmatchedRecordsInFile2(unmatchedRecordsInFile2)
                .matchPercentage(matchPercentage)
                .unmatchedTransactionPairs(unmatchedTransactionPairs)
                .build();
    }

}
