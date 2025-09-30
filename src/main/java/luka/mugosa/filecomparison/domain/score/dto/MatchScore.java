package luka.mugosa.filecomparison.domain.score.dto;

import java.util.Map;

public record MatchScore(double totalScore, MatchConfidence confidence, Map<String, Double> componentScores) {

    @Override
    public String toString() {
        return String.format("Score: %.1f, Confidence: %s, Components: %s",
                totalScore, confidence, componentScores);
    }
}