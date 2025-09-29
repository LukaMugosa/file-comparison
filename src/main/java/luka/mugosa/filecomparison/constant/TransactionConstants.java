package luka.mugosa.filecomparison.constant;

import java.time.format.DateTimeFormatter;

public final class TransactionConstants {

    private TransactionConstants() {
        // Prevent instantiation
    }

    // CSV Header Constants
    public static final String HEADER_PROFILE_NAME = "ProfileName";
    public static final String HEADER_TRANSACTION_DATE = "TransactionDate";
    public static final String HEADER_TRANSACTION_AMOUNT = "TransactionAmount";
    public static final String HEADER_TRANSACTION_NARRATIVE = "TransactionNarrative";
    public static final String HEADER_TRANSACTION_DESCRIPTION = "TransactionDescription";
    public static final String HEADER_TRANSACTION_ID = "TransactionID";
    public static final String HEADER_TRANSACTION_TYPE = "TransactionType";
    public static final String HEADER_WALLET_REFERENCE = "WalletReference";

    // Array of all required headers for validation
    public static final String[] REQUIRED_HEADERS = {
            HEADER_PROFILE_NAME,
            HEADER_TRANSACTION_DATE,
            HEADER_TRANSACTION_AMOUNT,
            HEADER_TRANSACTION_NARRATIVE,
            HEADER_TRANSACTION_DESCRIPTION,
            HEADER_TRANSACTION_ID,
            HEADER_TRANSACTION_TYPE,
            HEADER_WALLET_REFERENCE
    };
}