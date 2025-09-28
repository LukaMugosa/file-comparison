package luka.mugosa.filecomparison.domain.score.dto;

public enum MatchConfidence {
    HIGH,       // 80-100 points: Definitive match
    MEDIUM,     // 50-79 points: Strong potential match
    LOW,        // 20-49 points: Weak potential match
    VERY_LOW    // 0-19 points: Unlikely match
}