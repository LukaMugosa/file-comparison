package luka.mugosa.filecomparison.service;

import luka.mugosa.filecomparison.domain.dto.TransactionDto;
import luka.mugosa.filecomparison.domain.score.dto.MatchScore;

public interface ScoreService {
    MatchScore calculateScore(TransactionDto transaction1, TransactionDto transaction2);
}
