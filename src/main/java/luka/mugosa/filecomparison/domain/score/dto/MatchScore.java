package luka.mugosa.filecomparison.domain.score.dto;

import java.util.List;
import java.util.Map;

public class MatchScore {
    private final double totalScore;
    private final MatchConfidence confidence;
    private final Map<String, Double> componentScores;

    public MatchScore(double totalScore, MatchConfidence confidence,
                      Map<String, Double> componentScores) {
        this.totalScore = totalScore;
        this.confidence = confidence;
        this.componentScores = componentScores;
    }

    // Getters
    public double getTotalScore() {
        return totalScore;
    }

    public MatchConfidence getConfidence() {
        return confidence;
    }

    public Map<String, Double> getComponentScores() {
        return componentScores;
    }

    @Override
    public String toString() {
        return String.format("Score: %.1f, Confidence: %s, Components: %s",
                totalScore, confidence, componentScores);
    }
}