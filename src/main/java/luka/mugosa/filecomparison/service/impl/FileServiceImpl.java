package luka.mugosa.filecomparison.service.impl;

import luka.mugosa.filecomparison.constant.TransactionConstants;
import luka.mugosa.filecomparison.domain.dto.TransactionDto;
import luka.mugosa.filecomparison.domain.enumeration.TransactionType;
import luka.mugosa.filecomparison.domain.exception.CsvColumnMismatchException;
import luka.mugosa.filecomparison.domain.exception.EmptyFileException;
import luka.mugosa.filecomparison.domain.exception.FileParsingException;
import luka.mugosa.filecomparison.domain.exception.FileProcessingException;
import luka.mugosa.filecomparison.domain.exception.InvalidHeaderException;
import luka.mugosa.filecomparison.domain.exception.LineParsingException;
import luka.mugosa.filecomparison.domain.exception.MissingHeaderException;
import luka.mugosa.filecomparison.domain.exception.TransactionDataParsingException;
import luka.mugosa.filecomparison.domain.id.TransactionId;
import luka.mugosa.filecomparison.service.FileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public class FileServiceImpl implements FileService {

    private static final char CSV_SEPARATOR = ',';
    private static final Logger logger = LoggerFactory.getLogger(FileServiceImpl.class);

    public List<TransactionDto> parseFile(final String path) {
        logger.info("Starting file parsing for path: {}", path);
        final long startTime = System.currentTimeMillis();

        try {
            final List<TransactionDto> result = parseTransactionsCsv(path);
            final long duration = System.currentTimeMillis() - startTime;
            logger.info("Successfully parsed file: {} with {} transactions in {}ms",
                    path, result.size(), duration);
            return result;
        } catch (Exception e) {
            final long duration = System.currentTimeMillis() - startTime;
            logger.error("Failed to parse file: {} after {}ms", path, duration, e);
            throw e;
        }
    }

    public List<TransactionDto> parseTransactionsCsv(final String filePath) {
        logger.debug("Opening file for parsing: {}", filePath);

        try (final BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            logger.debug("File successfully opened, starting transaction parsing");
            return readTransactionRows(reader);
        } catch (IOException e) {
            logger.error("IO error while reading file: {}", filePath, e);
            throw new FileProcessingException("Failed to read file: " + filePath, e);
        }
    }

    private List<TransactionDto> readTransactionRows(final BufferedReader reader) throws IOException {
        logger.debug("Starting to read transaction rows from CSV");
        final List<TransactionDto> transactions = new ArrayList<>();

        final String headerLine = reader.readLine();
        if (headerLine == null) {
            logger.warn("CSV file is empty - no header line found");
            throw new EmptyFileException("CSV file is empty");
        }

        logger.debug("Header line read: {}", headerLine);

        final Map<String, Integer> headerMap = parseHeaders(headerLine);
        logger.info("Parsed {} headers: {}", headerMap.size(), headerMap.keySet());

        validateRequiredHeaders(headerMap);
        logger.debug("Header validation completed successfully");

        String line;
        int lineNumber = 1;
        int processedLines = 0;
        int skippedLines = 0;

        while ((line = reader.readLine()) != null) {
            lineNumber++;

            if (line.trim().isEmpty()) {
                logger.debug("Skipping empty line at line number: {}", lineNumber);
                skippedLines++;
                continue;
            }

            try {
                final TransactionDto transaction = parseTransactionLine(line, headerMap);
                transactions.add(transaction);
                processedLines++;

                if (processedLines % 1000 == 0) {
                    logger.info("Processed {} transactions so far...", processedLines);
                }

                logger.trace("Successfully parsed transaction at line {}: ID={}",
                        lineNumber, transaction.getTransactionID());

            } catch (FileParsingException e) {
                logger.error("Parsing error at line {}: {} - Line content: '{}'",
                        lineNumber, e.getMessage(), line);
                throw new LineParsingException(lineNumber, e.getMessage(), line, e);
            } catch (Exception e) {
                logger.error("Unexpected error at line {}: {} - Line content: '{}'",
                        lineNumber, e.getMessage(), line, e);
                throw new LineParsingException(lineNumber, "Unexpected parsing error", line, e);
            }
        }

        logger.info("CSV parsing completed - Total lines processed: {}, Successful: {}, Skipped: {}",
                lineNumber - 1, processedLines, skippedLines);
        logger.info("Total unique transactions parsed: {}", transactions.size());

        return transactions;
    }

    private TransactionDto parseTransactionLine(final String line, final Map<String, Integer> headerMap) {
        logger.trace("Parsing transaction line: {}", line);

        final String[] values = parseCsvLine(line, headerMap.size(), false);
        logger.trace("Split line into {} values", values.length);

//        We handled this case in parseCsvLine, if there is missing data we put ""
//        if (values.length != headerMap.size()) {
//            final String errorMsg = String.format("Expected %d columns but found %d",
//                    headerMap.size(), values.length);
//            logger.warn("Column count mismatch: {}", errorMsg);
//            throw new CsvColumnMismatchException(headerMap.size(), values.length);
//        }

        try {
            final String profileName = getValueByHeader(TransactionConstants.HEADER_PROFILE_NAME, values, headerMap);
            final ZonedDateTime transactionDate = parseDateTime(getValueByHeader(TransactionConstants.HEADER_TRANSACTION_DATE, values, headerMap));
            final Double transactionAmount = parseDouble(getValueByHeader(TransactionConstants.HEADER_TRANSACTION_AMOUNT, values, headerMap));
            final String transactionNarrative = getValueByHeader(TransactionConstants.HEADER_TRANSACTION_NARRATIVE, values, headerMap);
            final String transactionDescription = getValueByHeader(TransactionConstants.HEADER_TRANSACTION_DESCRIPTION, values, headerMap);
            final String transactionID = getValueByHeader(TransactionConstants.HEADER_TRANSACTION_ID, values, headerMap);
            final TransactionType transactionType = parseTransactionType(getValueByHeader(TransactionConstants.HEADER_TRANSACTION_TYPE, values, headerMap));
            final String walletReference = getValueByHeader(TransactionConstants.HEADER_WALLET_REFERENCE, values, headerMap);

            logger.trace("Successfully parsed transaction data: ID={}, Amount={}, Type={}",
                    transactionID, transactionAmount, transactionType);

            return new TransactionDto(
                    profileName, transactionDate, transactionAmount, transactionNarrative,
                    transactionDescription, new TransactionId(transactionID), transactionType, walletReference
            );

        } catch (FileParsingException e) {
            logger.debug("File parsing exception while processing transaction: {}", e.getMessage());
            throw e; // Re-throw our custom exceptions
        } catch (Exception e) {
            logger.error("Unexpected error while parsing transaction data", e);
            throw new TransactionDataParsingException(e.getMessage(), e);
        }
    }

    private ZonedDateTime parseDateTime(final String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            logger.debug("Empty or null date string provided, returning null");
            return null;
        }

        try {
            final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            final ZonedDateTime result = LocalDateTime.parse(dateStr, formatter)
                    .atZone(ZoneId.systemDefault());
            logger.trace("Successfully parsed date: '{}' to {}", dateStr, result);
            return result;
        } catch (DateTimeParseException e) {
            logger.warn("Failed to parse date: '{}' - {}", dateStr, e.getMessage());
            return null;
        }
    }

    private Double parseDouble(final String amountStr) {
        if (amountStr == null || amountStr.trim().isEmpty()) {
            logger.debug("Empty or null amount string provided, returning null");
            return null;
        }

        try {
            final Double result = Double.parseDouble(amountStr);
            logger.trace("Successfully parsed amount: '{}' to {}", amountStr, result);
            return result;
        } catch (NumberFormatException e) {
            logger.warn("Failed to parse amount: '{}' - returning null. Error: {}",
                    amountStr, e.getMessage());
            return null;
        }
    }

    private TransactionType parseTransactionType(final String typeStr) {
        if (typeStr == null) {
            logger.debug("Null transaction type provided, returning null");
            return null;
        }

        final String trimmedType = typeStr.trim();
        final TransactionType result = switch (trimmedType) {
            case "0" -> {
                logger.trace("Parsed transaction type '0' to TYPE_1");
                yield TransactionType.TYPE_1;
            }
            case "1" -> {
                logger.trace("Parsed transaction type '1' to TYPE_2");
                yield TransactionType.TYPE_2;
            }
            default -> {
                logger.warn("Unknown transaction type: '{}' - returning null", trimmedType);
                yield null;
            }
        };

        return result;
    }

    private String getValueByHeader(final String headerName, final String[] values,
                                    final Map<String, Integer> headerMap) {
        final Integer index = headerMap.get(headerName);

        if (index == null) {
            logger.error("Header '{}' not found in header map", headerName);
            throw new InvalidHeaderException(headerName);
        }

        if (index >= values.length) {
            logger.error("Header '{}' points to index {} but only {} values available",
                    headerName, index, values.length);
            throw new InvalidHeaderException(headerName);
        }

        final String value = values[index].trim();
        logger.trace("Retrieved value for header '{}' at index {}: '{}'", headerName, index, value);
        return value;
    }

    private void validateRequiredHeaders(final Map<String, Integer> headerMap) {
        logger.debug("Validating required headers");
        final String[] requiredHeaders = TransactionConstants.REQUIRED_HEADERS;

        for (final String required : requiredHeaders) {
            if (!headerMap.containsKey(required)) {
                logger.error("Missing required header: '{}'", required);
                logger.debug("Available headers: {}", headerMap.keySet());
                throw new MissingHeaderException(required);
            }
        }

        logger.debug("All {} required headers found", requiredHeaders.length);
    }

    private Map<String, Integer> parseHeaders(final String headerLine) {
        logger.debug("Parsing header line");
        final Map<String, Integer> headerMap = new HashMap<>();
        final String[] headers = parseCsvLine(headerLine, 0, true);

        logger.debug("Found {} header columns", headers.length);

        for (int i = 0; i < headers.length; i++) {
            final String header = headers[i].trim();
            headerMap.put(header, i);
            logger.trace("Mapped header '{}' to column index {}", header, i);
        }

        logger.debug("Successfully parsed header map with {} entries", headerMap.size());
        return headerMap;
    }

    private String[] parseCsvLine(final String line, final int headerCount, boolean isForHeader) {
        if (isForHeader) {
            return line.split(String.valueOf(CSV_SEPARATOR));
        }
        final String[] split = line.split(String.valueOf(CSV_SEPARATOR));
        final String[] result = new String[headerCount];
        for (int i = 0; i < headerCount; i++) {
            if (i < split.length) {
                result[i] = split[i];
            } else {
                result[i] = "";
            }
        }
        return result;
    }

    public List<TransactionDto> parseFile(final MultipartFile file) {
        final String filename = file.getOriginalFilename();
        final long fileSize = file.getSize();

        logger.info("Starting multipart file parsing - Filename: '{}', Size: {} bytes",
                filename, fileSize);

        if (fileSize == 0) {
            logger.warn("Uploaded file '{}' is empty", filename);
            throw new EmptyFileException("Uploaded file is empty");
        }

        final long maxFileSizeBytes = 10 * 1024 * 1024; // 10MB in bytes
        if (fileSize > maxFileSizeBytes) {
            logger.warn("Uploaded file '{}' exceeds size limit - Size: {} bytes, Limit: {} bytes",
                    filename, fileSize, maxFileSizeBytes);
            throw new IllegalArgumentException("File size should be less than 10MB");
        }

        final long startTime = System.currentTimeMillis();

        try {
            final BufferedReader reader = new BufferedReader(
                    new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));

            logger.debug("Created BufferedReader for multipart file with UTF-8 encoding");

            final List<TransactionDto> result = readTransactionRows(reader);
            final long duration = System.currentTimeMillis() - startTime;

            logger.info("Successfully processed multipart file '{}' with {} transactions in {}ms",
                    filename, result.size(), duration);

            return result;

        } catch (FileParsingException e) {
            final long duration = System.currentTimeMillis() - startTime;
            logger.error("File parsing error for '{}' after {}ms: {}",
                    filename, duration, e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            final long duration = System.currentTimeMillis() - startTime;
            logger.error("Unexpected error processing multipart file '{}' after {}ms",
                    filename, duration, e);
            throw new FileProcessingException("Failed to process uploaded file: " + filename, e);
        }
    }

    @Override
    public CompletableFuture<List<TransactionDto>> parseFileAsync(String path) {
        return CompletableFuture.supplyAsync(() -> parseFile(path));
    }

    @Override
    public CompletableFuture<List<TransactionDto>> parseFileAsync(MultipartFile file) {
        return CompletableFuture.supplyAsync(() -> parseFile(file));
    }
}