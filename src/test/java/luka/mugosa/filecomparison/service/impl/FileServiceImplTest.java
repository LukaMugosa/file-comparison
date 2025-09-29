package luka.mugosa.filecomparison.service.impl;

import luka.mugosa.filecomparison.constant.TransactionConstants;
import luka.mugosa.filecomparison.domain.dto.TransactionDto;
import luka.mugosa.filecomparison.domain.enumeration.TransactionType;
import luka.mugosa.filecomparison.domain.exception.EmptyFileException;
import luka.mugosa.filecomparison.domain.exception.FileProcessingException;
import luka.mugosa.filecomparison.domain.exception.LineParsingException;
import luka.mugosa.filecomparison.domain.exception.MissingHeaderException;
import luka.mugosa.filecomparison.domain.id.TransactionId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FileServiceImplTest {

    private FileServiceImpl fileService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        fileService = new FileServiceImpl();
    }

    @Test
    void parseFile_WithValidCsvFile_ShouldReturnTransactions() throws IOException {
        final String csvContent = createValidCsvContent();
        final Path testFile = createTempCsvFile(csvContent);

        final List<TransactionDto> result = fileService.parseFile(testFile.toString());

        assertThat(result)
                .isNotNull()
                .hasSize(3);

        final TransactionDto transaction1 = result.stream()
                .filter(t -> new TransactionId("0584011808649511").equals(t.getTransactionID()))
                .findFirst()
                .orElseThrow();

        assertThat(transaction1.getProfileName()).isEqualTo("Card Campaign");
        assertThat(transaction1.getTransactionAmount()).isEqualTo(-20000.0);
        assertThat(transaction1.getTransactionType()).isEqualTo(TransactionType.TYPE_2);
        assertThat(transaction1.getTransactionNarrative()).isEqualTo("*MOLEPS ATM25             MOLEPOLOLE    BW");
        assertThat(transaction1.getTransactionDescription()).isEqualTo("DEDUCT");
        assertThat(transaction1.getWalletReference()).isEqualTo("P_NzI2ODY2ODlfMTM4MjcwMTU2NS45MzA5");

        final TransactionDto transaction2 = result.stream()
                .filter(t -> new TransactionId("0584011815513406").equals(t.getTransactionID()))
                .findFirst()
                .orElseThrow();

        assertThat(transaction2.getProfileName()).isEqualTo("Card Campaign");
        assertThat(transaction2.getTransactionAmount()).isEqualTo(-10000.0);
        assertThat(transaction2.getTransactionType()).isEqualTo(TransactionType.TYPE_2);
        assertThat(transaction2.getTransactionNarrative()).isEqualTo("*MOGODITSHANE2            MOGODITHSANE  BW");
        assertThat(transaction2.getTransactionDescription()).isEqualTo("DEDUCT");
        assertThat(transaction2.getWalletReference()).isEqualTo("P_NzI1MjA1NjZfMTM3ODczODI3Mi4wNzY5");

        final TransactionDto transaction3 = result.stream()
                .filter(t -> new TransactionId("0084012233581869").equals(t.getTransactionID()))
                .findFirst()
                .orElseThrow();

        assertThat(transaction3.getProfileName()).isEqualTo("Card Campaign");
        assertThat(transaction3.getTransactionAmount()).isEqualTo(-20000.0);
        assertThat(transaction3.getTransactionType()).isEqualTo(TransactionType.TYPE_1);
        assertThat(transaction3.getTransactionNarrative()).isEqualTo("Molepolole Filli100558    Gaborone      BW");
        assertThat(transaction3.getTransactionDescription()).isEqualTo("DEDUCT");
        assertThat(transaction3.getWalletReference()).isEqualTo("P_NzI5OTE3NjZfMTM4MTkzNjk5Mi45NTc2");
    }

    @Test
    void parseFile_WithNonExistentFile_ShouldThrowFileProcessingException() {
        final String nonExistentPath = "/path/that/does/not/exist.csv";

        assertThatThrownBy(() -> fileService.parseFile(nonExistentPath))
                .isInstanceOf(FileProcessingException.class)
                .hasMessageContaining("Failed to process file: Failed to read file: /path/that/does/not/exist.csv");
    }

    @Test
    void parseFile_WithEmptyFile_ShouldThrowEmptyFileException() throws IOException {
        final Path emptyFile = createTempCsvFile("");

        assertThatThrownBy(() -> fileService.parseFile(emptyFile.toString()))
                .isInstanceOf(EmptyFileException.class)
                .hasMessageContaining("CSV file is empty");
    }

    @Test
    void parseFile_WithOnlyHeaderLine_ShouldReturnEmptySet() throws IOException {
        final String csvContent = createCsvHeader();
        final Path testFile = createTempCsvFile(csvContent);

        final List<TransactionDto> result = fileService.parseFile(testFile.toString());

        assertThat(result).isEmpty();
    }

    @Test
    void parseFile_WithMissingRequiredHeaders_ShouldThrowFileProcessingException() throws IOException {
        final String csvContent = "ProfileName,TransactionAmount\nJohn,100.50";
        final Path testFile = createTempCsvFile(csvContent);

        assertThatThrownBy(() -> fileService.parseFile(testFile.toString()))
                .isInstanceOf(MissingHeaderException.class)
                .hasMessageContaining("Missing required header: TransactionDate");
    }

    @Test
    void parseFile_WithValidMultipartFile_ShouldReturnTransactions() {
        final String csvContent = createValidCsvContent();
        final MultipartFile mockFile = new MockMultipartFile(
                "file",
                "test.csv",
                "text/csv",
                csvContent.getBytes()
        );

        final List<TransactionDto> result = fileService.parseFile(mockFile);

        assertThat(result)
                .isNotNull()
                .hasSize(3);

        final TransactionDto transaction1 = result.stream()
                .filter(t -> new TransactionId("0584011808649511").equals(t.getTransactionID()))
                .findFirst()
                .orElseThrow();

        assertThat(transaction1.getProfileName()).isEqualTo("Card Campaign");
        assertThat(transaction1.getTransactionAmount()).isEqualTo(-20000.0);
        assertThat(transaction1.getTransactionType()).isEqualTo(TransactionType.TYPE_2);
        assertThat(transaction1.getTransactionNarrative()).isEqualTo("*MOLEPS ATM25             MOLEPOLOLE    BW");
        assertThat(transaction1.getTransactionDescription()).isEqualTo("DEDUCT");
        assertThat(transaction1.getWalletReference()).isEqualTo("P_NzI2ODY2ODlfMTM4MjcwMTU2NS45MzA5");

        final TransactionDto transaction2 = result.stream()
                .filter(t -> new TransactionId("0584011815513406").equals(t.getTransactionID()))
                .findFirst()
                .orElseThrow();

        assertThat(transaction2.getProfileName()).isEqualTo("Card Campaign");
        assertThat(transaction2.getTransactionAmount()).isEqualTo(-10000.0);
        assertThat(transaction2.getTransactionType()).isEqualTo(TransactionType.TYPE_2);
        assertThat(transaction2.getTransactionNarrative()).isEqualTo("*MOGODITSHANE2            MOGODITHSANE  BW");
        assertThat(transaction2.getTransactionDescription()).isEqualTo("DEDUCT");
        assertThat(transaction2.getWalletReference()).isEqualTo("P_NzI1MjA1NjZfMTM3ODczODI3Mi4wNzY5");

        final TransactionDto transaction3 = result.stream()
                .filter(t -> new TransactionId("0084012233581869").equals(t.getTransactionID()))
                .findFirst()
                .orElseThrow();

        assertThat(transaction3.getProfileName()).isEqualTo("Card Campaign");
        assertThat(transaction3.getTransactionAmount()).isEqualTo(-20000.0);
        assertThat(transaction3.getTransactionType()).isEqualTo(TransactionType.TYPE_1);
        assertThat(transaction3.getTransactionNarrative()).isEqualTo("Molepolole Filli100558    Gaborone      BW");
        assertThat(transaction3.getTransactionDescription()).isEqualTo("DEDUCT");
        assertThat(transaction3.getWalletReference()).isEqualTo("P_NzI5OTE3NjZfMTM4MTkzNjk5Mi45NTc2");
    }

    @Test
    void parseFile_WithEmptyMultipartFile_ShouldThrowEmptyFileException() {
        final MultipartFile emptyFile = new MockMultipartFile(
                "file",
                "empty.csv",
                "text/csv",
                "".getBytes()
        );

        assertThatThrownBy(() -> fileService.parseFile(emptyFile))
                .isInstanceOf(EmptyFileException.class)
                .hasMessageContaining("Uploaded file is empty");
    }

    @Test
    void parseFile_WithInvalidMultipartFileData_ShouldThrowLineParsingException() {
        final String invalidCsvContent = createCsvHeader() + "\nInvalid,Data,Row,Missing,Fields";
        final MultipartFile invalidFile = new MockMultipartFile(
                "file",
                "invalid.csv",
                "text/csv",
                invalidCsvContent.getBytes()
        );

        assertThatThrownBy(() -> fileService.parseFile(invalidFile))
                .isInstanceOf(LineParsingException.class)
                .hasMessageContaining("Error parsing line 2: Expected 8 columns but found 5. Line content: Invalid,Data,Row,Missing,Fields");
    }

    @Test
    void parseFile_WithMultipartFileContainingEmptyLines_ShouldSkipEmptyLines() {
        final String csvContentWithEmptyLines = createCsvHeader() + "\n" +
                "Card Campaign,2014-01-11 22:27:44,-20000,*MOLEPS ATM25             MOLEPOLOLE    BW,DEDUCT,0584011808649511,1,P_NzI2ODY2ODlfMTM4MjcwMTU2NS45MzA5\n" +
                "\n" +
                "Card Campaign,2014-01-11 22:39:11,-10000,*MOGODITSHANE2            MOGODITHSANE  BW,DEDUCT,0584011815513406,1,P_NzI1MjA1NjZfMTM3ODczODI3Mi4wNzY5\n" +
                "\n";

        final MultipartFile fileWithEmptyLines = new MockMultipartFile(
                "file",
                "test.csv",
                "text/csv",
                csvContentWithEmptyLines.getBytes()
        );

        final List<TransactionDto> result = fileService.parseFile(fileWithEmptyLines);

        assertThat(result).hasSize(2);
    }

    @Test
    void parseFile_WithMultipartFileContainingNullAmounts_ShouldHandleGracefully() {
        final String csvContent = createCsvHeader() + "\n" +
                "Card Campaign,2014-01-11 22:27:44,,*MOLEPS ATM25             MOLEPOLOLE    BW,DEDUCT,0584011808649511,1,P_NzI2ODY2ODlfMTM4MjcwMTU2NS45MzA5\n" +
                "Card Campaign,2014-01-11 22:39:11,invalid_amount,*MOGODITSHANE2            MOGODITHSANE  BW,DEDUCT,0584011815513406,1,P_NzI1MjA1NjZfMTM3ODczODI3Mi4wNzY5";

        final MultipartFile fileWithNullAmounts = new MockMultipartFile(
                "file",
                "test.csv",
                "text/csv",
                csvContent.getBytes()
        );

        final List<TransactionDto> result = fileService.parseFile(fileWithNullAmounts);

        assertThat(result).hasSize(2);

        result.forEach(transaction -> {
            if ("0584011808649511".equals(transaction.getTransactionID())) {
                assertThat(transaction.getTransactionAmount()).isNull();
            } else if ("0584011815513406".equals(transaction.getTransactionID())) {
                assertThat(transaction.getTransactionAmount()).isNull();
            }
        });
    }

    @Test
    void parseFile_WithMultipartFileContainingDifferentTransactionTypes_ShouldParseCorrectly() {
        final String csvContent = createCsvHeader() + "\n" +
                "Card Campaign,2014-01-12 06:26:17,-20000,Molepolole Filli100558    Gaborone      BW,DEDUCT,0084012233581869,0,P_NzI5OTE3NjZfMTM4MTkzNjk5Mi45NTc2\n" +
                "Card Campaign,2014-01-11 22:27:44,-20000,*MOLEPS ATM25             MOLEPOLOLE    BW,DEDUCT,0584011808649511,1,P_NzI2ODY2ODlfMTM4MjcwMTU2NS45MzA5\n" +
                "Card Campaign,2014-01-12 09:17:17,-30000,*TONOTA                   FRANCISTOWN   BW,DEDUCT,0464012334375636,2,P_NzIyMzY0ODRfMTM4NzM3NjIzMi40MDg=";

        final MultipartFile file = new MockMultipartFile(
                "file",
                "test.csv",
                "text/csv",
                csvContent.getBytes()
        );

        final List<TransactionDto> result = fileService.parseFile(file);

        assertThat(result).hasSize(3);

        final long type1Count = result.stream()
                .filter(t -> TransactionType.TYPE_1.equals(t.getTransactionType()))
                .count();
        final long type2Count = result.stream()
                .filter(t -> TransactionType.TYPE_2.equals(t.getTransactionType()))
                .count();
        final long nullTypeCount = result.stream()
                .filter(t -> t.getTransactionType() == null)
                .count();

        assertThat(type1Count).isEqualTo(1);
        assertThat(type2Count).isEqualTo(1);
        assertThat(nullTypeCount).isEqualTo(1);
    }

    @Test
    void parseFile_WithInvalidDateFormat_ShouldThrowLineParsingException() {
        final String csvContent = createCsvHeader() + "\n" +
                "Card Campaign,invalid-date-format,-20000,*MOLEPS ATM25             MOLEPOLOLE    BW,DEDUCT,0584011808649511,1,P_NzI2ODY2ODlfMTM4MjcwMTU2NS45MzA5";

        final MultipartFile file = new MockMultipartFile(
                "file",
                "test.csv",
                "text/csv",
                csvContent.getBytes()
        );

        final List<TransactionDto> result = fileService.parseFile(file);

        assertThat(result).hasSize(1);
        final long nullCount = result.stream()
                .filter(t -> Objects.isNull(t.getTransactionDate()))
                .count();

        assertThat(nullCount).isEqualTo(1);
    }

    @Test
    void parseFile_WithNullTransactionType_ShouldThrowLineParsingException() {
        final String csvContent = createCsvHeader() + "\n" +
                "Card Campaign,2014-01-11 22:27:44,-20000,*MOLEPS ATM25             MOLEPOLOLE    BW,DEDUCT,0584011808649511,,P_NzI2ODY2ODlfMTM4MjcwMTU2NS45MzA5";

        final MultipartFile file = new MockMultipartFile(
                "file",
                "test.csv",
                "text/csv",
                csvContent.getBytes()
        );

        final List<TransactionDto> result = fileService.parseFile(file);

        assertThat(result).hasSize(1);

        final long nullTypeCount = result.stream()
                .filter(t -> t.getTransactionType() == null)
                .count();

        assertThat(nullTypeCount).isEqualTo(1);
    }

    @Test
    void parseFile_WithFileSizeOver10MB_ShouldThrowIllegalArgumentException() {
        final long fileSize = 11 * 1024 * 1024; // 11MB
        final MultipartFile largeFile = new MockMultipartFile(
                "file",
                "large-file.csv",
                "text/csv",
                new byte[(int) fileSize]
        );

        assertThatThrownBy(() -> fileService.parseFile(largeFile))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("File size should be less than 10MB");
    }

    @Test
    void parseFile_WithFileSizeExactly10MB_ShouldParseSuccessfully() {
        final long fileSize = 10 * 1024 * 1024; // Exactly 10MB
        final String csvContent = createValidCsvContent();
        final MultipartFile file = new MockMultipartFile(
                "file",
                "file.csv",
                "text/csv",
                csvContent.getBytes()
        );

        final List<TransactionDto> result = fileService.parseFile(file);

        assertThat(result)
                .isNotNull()
                .hasSize(3);
    }

    @Test
    void parseFile_WithFileSizeJustUnder10MB_ShouldParseSuccessfully() {
        final String csvContent = createValidCsvContent();
        final MultipartFile file = new MockMultipartFile(
                "file",
                "file.csv",
                "text/csv",
                csvContent.getBytes()
        );

        final List<TransactionDto> result = fileService.parseFile(file);

        assertThat(result)
                .isNotNull()
                .hasSize(3);
    }

    private String createValidCsvContent() {
        return createCsvHeader() + "\n" +
                "Card Campaign,2014-01-11 22:27:44,-20000,*MOLEPS ATM25             MOLEPOLOLE    BW,DEDUCT,0584011808649511,1,P_NzI2ODY2ODlfMTM4MjcwMTU2NS45MzA5\n" +
                "Card Campaign,2014-01-11 22:39:11,-10000,*MOGODITSHANE2            MOGODITHSANE  BW,DEDUCT,0584011815513406,1,P_NzI1MjA1NjZfMTM3ODczODI3Mi4wNzY5\n" +
                "Card Campaign,2014-01-12 06:26:17,-20000,Molepolole Filli100558    Gaborone      BW,DEDUCT,0084012233581869,0,P_NzI5OTE3NjZfMTM4MTkzNjk5Mi45NTc2";
    }

    private String createCsvHeader() {
        return String.join(",",
                TransactionConstants.HEADER_PROFILE_NAME,
                TransactionConstants.HEADER_TRANSACTION_DATE,
                TransactionConstants.HEADER_TRANSACTION_AMOUNT,
                TransactionConstants.HEADER_TRANSACTION_NARRATIVE,
                TransactionConstants.HEADER_TRANSACTION_DESCRIPTION,
                TransactionConstants.HEADER_TRANSACTION_ID,
                TransactionConstants.HEADER_TRANSACTION_TYPE,
                TransactionConstants.HEADER_WALLET_REFERENCE
        );
    }

    private Path createTempCsvFile(final String content) throws IOException {
        final Path tempFile = tempDir.resolve("test.csv");
        Files.write(tempFile, content.getBytes());
        return tempFile;
    }
}