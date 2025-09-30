package luka.mugosa.filecomparison.domain.score.dto;

public class ScoringWeights {

    private ScoringWeights() {
        throw new IllegalStateException("Utility class");
    }

    public static final double TRANSACTION_ID_WEIGHT = 40.0;
    public static final double AMOUNT_EXACT_WEIGHT = 25.0;
    public static final double DATE_EXACT_WEIGHT = 15.0;
    public static final double WALLET_REFERENCE_WEIGHT = 10.0;
    public static final double NARRATIVE_SIMILARITY_WEIGHT = 5.0;
    public static final double DESCRIPTION_SIMILARITY_WEIGHT = 3.0;
    public static final double TRANSACTION_TYPE_WEIGHT = 2.0;

    // Tolerance scoring weights
    public static final double AMOUNT_TOLERANCE_WEIGHT = 15.0;
    public static final double DATE_TOLERANCE_WEIGHT = 8.0;
}