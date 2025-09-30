# File Reconciliation Application

A Spring Boot application for reconciling financial transactions between two CSV files. The application compares transaction data, identifies matches based on a sophisticated scoring algorithm, and reports discrepancies.

## Table of Contents

- [Features](#features)
- [Prerequisites](#prerequisites)
- [Installation](#installation)
- [Running the Application](#running-the-application)
- [API Documentation](#api-documentation)
- [CSV File Format](#csv-file-format)
- [Configuration](#configuration)
- [Testing](#testing)
- [Troubleshooting](#troubleshooting)

## Features

- **Parallel File Processing**: Processes two CSV files simultaneously using virtual threads for optimal performance
- **Intelligent Matching**: Multi-factor scoring algorithm that considers:
    - Transaction ID
    - Amount (with 1% tolerance)
    - Date (with ±2 days tolerance)
    - Transaction narrative and description
    - Wallet reference
    - Transaction type
- **Duplicate Handling**: Properly handles duplicate transactions with the same ID
- **Comprehensive Reporting**: Detailed reconciliation results with matched and unmatched transactions
- **File Size Validation**: Enforces 10MB maximum file size limit
- **Error Handling**: Robust exception handling with detailed error messages

## Prerequisites

- **Java 21** or higher
- **Maven 3.8+**
- Minimum 512MB RAM (recommended 1GB for large files)

## Installation

### 1. Clone the Repository

```bash
git clone <repository-url>
cd file-comparison
```

### 2. Build the Application

```bash
mvn clean install
```

This will:
- Compile the source code
- Run all unit tests
- Package the application as a JAR file

## Running the Application

### Option 1: Using Maven (Development)

```bash
mvn spring-boot:run
```

### Option 2: Using the JAR File (Production)

```bash
java -jar target/file-comparison-1.0.0.jar
```

### Option 3: With Custom Configuration

```bash
java -jar target/file-comparison-1.0.0.jar --server.port=8090
```

The application will start on `http://localhost:8080` by default.

You should see output like:
```
Started FileComparisonApplication in 2.345 seconds
```

## API Documentation

### Reconcile Transactions

**Endpoint:** `POST /api/v1/reconcile-transactions`

**Content-Type:** `multipart/form-data`

**Parameters:**
- `file1` (required): First CSV file (Paymentology format)
- `file2` (required): Second CSV file (Client format)

**File Constraints:**
- Maximum file size: 10MB per file
- Format: CSV with specific headers (see below)
- Encoding: UTF-8

**Example Request using cURL:**

```bash
curl -X POST http://localhost:8080/api/v1/reconcile-transactions \
  -F "file1=@PaymentologyMarkoffFile20140113.csv" \
  -F "file2=@ClientMarkoffFile20140113.csv"
```

**Example Request using Postman:**

1. Select `POST` method
2. Enter URL: `http://localhost:8080/api/v1/reconcile-transactions`
3. Go to "Body" tab
4. Select "form-data"
5. Add two keys:
    - Key: `file1`, Type: File, Value: Select your first CSV
    - Key: `file2`, Type: File, Value: Select your second CSV
6. Click "Send"

**Response Example:**

```json
{
  "totalRecordsInFile1": 100,
  "totalRecordsInFile2": 98,
  "matchedRecords": 85,
  "unmatchedRecordsInFile1": 15,
  "unmatchedRecordsInFile2": 13,
  "matchPercentage": 85.0,
  "processingTimeMs": 1234,
  "unmatchedTransactionPairs": [
    {
      "transaction1": {
        "profileName": "Card Campaign",
        "transactionDate": "2014-01-11T22:27:44Z",
        "transactionAmount": -20000.0,
        "transactionNarrative": "*MOLEPS ATM25 MOLEPOLOLE BW",
        "transactionDescription": "DEDUCT",
        "transactionID": "0584011808649511",
        "transactionType": "TYPE_2",
        "walletReference": "P_NzI2ODY2ODlfMTM4MjcwMTU2NS45MzA5"
      },
      "transaction2": null
    }
  ]
}
```

## CSV File Format

### Required Headers (in any order)

The CSV files must contain these exact header names:

```
ProfileName,TransactionDate,TransactionAmount,TransactionNarrative,TransactionDescription,TransactionID,TransactionType,WalletReference
```

### Header Descriptions

| Header | Description | Format | Example |
|--------|-------------|--------|---------|
| ProfileName | Account profile name | String | Card Campaign |
| TransactionDate | Transaction timestamp | yyyy-MM-dd HH:mm:ss | 2014-01-11 22:27:44 |
| TransactionAmount | Amount (negative for debits) | Decimal | -20000.0 |
| TransactionNarrative | Transaction description | String | *MOLEPS ATM25 MOLEPOLOLE BW |
| TransactionDescription | Transaction type description | String | DEDUCT |
| TransactionID | Unique transaction identifier | String | 0584011808649511 |
| TransactionType | Transaction category | 0 or 1 | 1 |
| WalletReference | Wallet reference code | String | P_NzI2ODY2ODlfMTM4MjcwMTU2NS45MzA5 |

### Sample CSV Data

```csv
ProfileName,TransactionDate,TransactionAmount,TransactionNarrative,TransactionDescription,TransactionID,TransactionType,WalletReference
Card Campaign,2014-01-11 22:27:44,-20000,*MOLEPS ATM25 MOLEPOLOLE BW,DEDUCT,0584011808649511,1,P_NzI2ODY2ODlfMTM4MjcwMTU2NS45MzA5
Card Campaign,2014-01-11 22:39:11,-10000,*MOGODITSHANE2 MOGODITHSANE BW,DEDUCT,0584011815513406,1,P_NzI1MjA1NjZfMTM3ODczODI3Mi4wNzY5
```

### Notes

- Empty fields are allowed (will be treated as null)
- Duplicate transactions (same TransactionID) are supported
- File must have at least a header row
- Lines with incorrect column counts will be rejected

## Configuration

### Application Properties

Create or modify `src/main/resources/application.properties`:

```properties
# Server Configuration
server.port=8080

# File Upload Configuration
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=20MB

# Logging Configuration
logging.level.luka.mugosa.filecomparison=INFO
logging.level.org.springframework=WARN

# Virtual Threads (Java 21+)
spring.threads.virtual.enabled=true
```

### Adjusting Memory

For large files, increase JVM heap size:

```bash
java -Xmx2G -jar target/file-comparison-1.0.0.jar
```

### Custom Scoring Weights

Modify `ScoringWeights.java` to adjust matching criteria:

```java
public class ScoringWeights {
    public static final double TRANSACTION_ID_WEIGHT = 40.0;
    public static final double AMOUNT_EXACT_WEIGHT = 25.0;
    public static final double DATE_EXACT_WEIGHT = 15.0;
    // ... adjust as needed
}
```

## Testing

### Run All Tests

```bash
mvn test
```

### Run Specific Test Class

```bash
mvn test -Dtest=ComparisonServiceImplTest
```

### Run with Coverage Report

```bash
mvn clean test jacoco:report
```

Coverage report will be available at: `target/site/jacoco/index.html`

### Test Files

Sample test CSV files are located in `src/test/resources/`:
- `test-file-1.csv`
- `test-file-2.csv`

## Troubleshooting

### Common Issues

#### 1. Application Won't Start

**Error:** `Port 8080 is already in use`

**Solution:**
```bash
# Change port
java -jar target/file-comparison-1.0.0.jar --server.port=8090
```

#### 2. File Size Exceeded

**Error:** `File size should be less than 10MB`

**Solution:**
- Split large CSV files into smaller chunks
- Or increase limit in `application.properties`:
```properties
spring.servlet.multipart.max-file-size=50MB
```

#### 3. Missing Headers

**Error:** `Missing required header: TransactionDate`

**Solution:** Ensure CSV file has exact header names (case-sensitive)

#### 4. Out of Memory

**Error:** `java.lang.OutOfMemoryError: Java heap space`

**Solution:**
```bash
java -Xmx2G -jar target/file-comparison-1.0.0.jar
```

#### 5. Parsing Timeout

**Error:** `File parsing operation timed out`

**Solution:** The application has a 2-minute timeout for file parsing. For very large files:
- Ensure files are not corrupted
- Check system resources
- Consider splitting files

### Logging

Enable debug logging for troubleshooting:

```properties
logging.level.luka.mugosa.filecomparison=DEBUG
```

View logs in console or configure file logging:

```properties
logging.file.name=application.log
logging.file.path=/var/logs
```