package luka.mugosa.filecomparison.service.impl;

import luka.mugosa.filecomparison.domain.dto.TransactionDto;
import luka.mugosa.filecomparison.domain.dto.response.ReconciliationResponse;
import luka.mugosa.filecomparison.domain.exception.FileProcessingException;
import luka.mugosa.filecomparison.service.FileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;

import static luka.mugosa.filecomparison.service.util.TransactionUtil.createLargeTransactionSet;
import static luka.mugosa.filecomparison.service.util.TransactionUtil.createTransactionSet;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionServiceImplTest {

    @Mock
    private ComparisonServiceImpl comparisonService;

    @Mock
    private FileService fileService;

    @Mock
    private MultipartFile file1;

    @Mock
    private MultipartFile file2;

    private TransactionServiceImpl transactionService;

    @BeforeEach
    void setUp() {
        transactionService = new TransactionServiceImpl(comparisonService, fileService);
    }

    @Test
    @DisplayName("Should successfully reconcile transactions when both files parse correctly")
    void shouldSuccessfullyReconcileTransactionsWhenBothFilesParse() {
        // Arrange
        final List<TransactionDto> collection1 = createTransactionSet("TXN001", "TXN002");
        final List<TransactionDto> collection2 = createTransactionSet("TXN001", "TXN002");

        final CompletableFuture<List<TransactionDto>> future1 = CompletableFuture.completedFuture(collection1);
        final CompletableFuture<List<TransactionDto>> future2 = CompletableFuture.completedFuture(collection2);

        when(fileService.parseFileAsync(file1)).thenReturn(future1);
        when(fileService.parseFileAsync(file2)).thenReturn(future2);

        final ReconciliationResponse expectedResponse = ReconciliationResponse.builder()
                .totalRecordsInFile1(2)
                .totalRecordsInFile2(2)
                .matchedRecords(2)
                .unmatchedRecordsInFile1(0)
                .unmatchedRecordsInFile2(0)
                .matchPercentage(100.0)
                .build();

        when(comparisonService.compareData(collection1, collection2)).thenReturn(expectedResponse);

        // Act
        final ReconciliationResponse response = transactionService.reconcileTransactions(file1, file2);

        // Assert
        assertNotNull(response);
        assertEquals(2, response.totalRecordsInFile1());
        assertEquals(2, response.totalRecordsInFile2());
        assertEquals(2, response.matchedRecords());
        assertEquals(0, response.unmatchedRecordsInFile1());
        assertEquals(0, response.unmatchedRecordsInFile2());
        assertEquals(100.0, response.matchPercentage());

        verify(fileService, times(1)).parseFileAsync(file1);
        verify(fileService, times(1)).parseFileAsync(file2);
        verify(comparisonService, times(1)).compareData(collection1, collection2);
    }

    @Test
    @DisplayName("Should throw FileProcessingException when file parsing times out")
    @Disabled("This test is slow, no need to run it in group")
    void shouldThrowFileProcessingExceptionWhenFileParsingTimesOut() {
        // Arrange
        final CompletableFuture<List<TransactionDto>> slowFuture = new CompletableFuture<>();
        // Don't complete the future - simulates timeout

        when(fileService.parseFileAsync(file1)).thenReturn(slowFuture);
        when(fileService.parseFileAsync(file2)).thenReturn(slowFuture);

        // Act & Assert
        final FileProcessingException exception = assertThrows(
                FileProcessingException.class,
                () -> transactionService.reconcileTransactions(file1, file2)
        );

        assertNotNull(exception);
        assertEquals("Failed to process file: File parsing operation timed out", exception.getMessage());
        assertInstanceOf(TimeoutException.class, exception.getCause());

        verify(fileService, times(1)).parseFileAsync(file1);
        verify(fileService, times(1)).parseFileAsync(file2);
        verifyNoInteractions(comparisonService);
    }

    @Test
    @DisplayName("Should throw FileProcessingException when file parsing fails with execution exception")
    void shouldThrowFileProcessingExceptionWhenFileParsingFails() {
        // Arrange
        final RuntimeException cause = new RuntimeException("File parsing error");
        final CompletableFuture<List<TransactionDto>> failedFuture = CompletableFuture.failedFuture(cause);

        when(fileService.parseFileAsync(file1)).thenReturn(failedFuture);
        when(fileService.parseFileAsync(file2)).thenReturn(CompletableFuture.completedFuture(createTransactionSet()));

        // Act & Assert
        final FileProcessingException exception = assertThrows(
                FileProcessingException.class,
                () -> transactionService.reconcileTransactions(file1, file2)
        );

        assertNotNull(exception);
        assertEquals("Failed to process file: File parsing failed", exception.getMessage());
        assertEquals(cause, exception.getCause());

        verify(fileService, times(1)).parseFileAsync(file1);
        verify(fileService, times(1)).parseFileAsync(file2);
        verifyNoInteractions(comparisonService);
    }

    @Test
    @DisplayName("Should successfully reconcile empty transaction sets")
    void shouldSuccessfullyReconcileEmptyTransactionSets() {
        // Arrange
        final List<TransactionDto> emptyCollection1 = new ArrayList<>();
        final List<TransactionDto> emptyCollection2 = new ArrayList<>();

        final CompletableFuture<List<TransactionDto>> future1 = CompletableFuture.completedFuture(emptyCollection1);
        final CompletableFuture<List<TransactionDto>> future2 = CompletableFuture.completedFuture(emptyCollection2);

        when(fileService.parseFileAsync(file1)).thenReturn(future1);
        when(fileService.parseFileAsync(file2)).thenReturn(future2);

        final ReconciliationResponse expectedResponse = ReconciliationResponse.builder()
                .totalRecordsInFile1(0)
                .totalRecordsInFile2(0)
                .matchedRecords(0)
                .unmatchedRecordsInFile1(0)
                .unmatchedRecordsInFile2(0)
                .matchPercentage(0.0)
                .build();

        when(comparisonService.compareData(emptyCollection1, emptyCollection2)).thenReturn(expectedResponse);

        // Act
        final ReconciliationResponse response = transactionService.reconcileTransactions(file1, file2);

        // Assert
        assertNotNull(response);
        assertEquals(0, response.totalRecordsInFile1());
        assertEquals(0, response.totalRecordsInFile2());
        assertEquals(0, response.matchedRecords());
        assertEquals(0, response.unmatchedRecordsInFile1());
        assertEquals(0, response.unmatchedRecordsInFile2());
        assertEquals(0.0, response.matchPercentage());

        verify(fileService, times(1)).parseFileAsync(file1);
        verify(fileService, times(1)).parseFileAsync(file2);
        verify(comparisonService, times(1)).compareData(emptyCollection1, emptyCollection2);
    }

    @Test
    @DisplayName("Should successfully handle one file with transactions and one empty")
    void shouldSuccessfullyHandleOneFileWithTransactionsAndOneEmpty() {
        // Arrange
        final List<TransactionDto> collection1 = createTransactionSet("TXN001", "TXN002");
        final List<TransactionDto> emptyCollection2 = new ArrayList<>();

        final CompletableFuture<List<TransactionDto>> future1 = CompletableFuture.completedFuture(collection1);
        final CompletableFuture<List<TransactionDto>> future2 = CompletableFuture.completedFuture(emptyCollection2);

        when(fileService.parseFileAsync(file1)).thenReturn(future1);
        when(fileService.parseFileAsync(file2)).thenReturn(future2);

        final ReconciliationResponse expectedResponse = ReconciliationResponse.builder()
                .totalRecordsInFile1(2)
                .totalRecordsInFile2(0)
                .matchedRecords(0)
                .unmatchedRecordsInFile1(2)
                .unmatchedRecordsInFile2(0)
                .matchPercentage(0.0)
                .build();

        when(comparisonService.compareData(collection1, emptyCollection2)).thenReturn(expectedResponse);

        // Act
        final ReconciliationResponse response = transactionService.reconcileTransactions(file1, file2);

        // Assert
        assertNotNull(response);
        assertEquals(2, response.totalRecordsInFile1());
        assertEquals(0, response.totalRecordsInFile2());
        assertEquals(0, response.matchedRecords());
        assertEquals(2, response.unmatchedRecordsInFile1());
        assertEquals(0, response.unmatchedRecordsInFile2());
        assertEquals(0.0, response.matchPercentage());

        verify(fileService, times(1)).parseFileAsync(file1);
        verify(fileService, times(1)).parseFileAsync(file2);
        verify(comparisonService, times(1)).compareData(collection1, emptyCollection2);
    }

    @Test
    @DisplayName("Should successfully handle large transaction sets")
    void shouldSuccessfullyHandleLargeTransactionSets() {
        // Arrange
        final List<TransactionDto> largeCollection1 = createLargeTransactionSet(1000);
        final List<TransactionDto> largeCollection2 = createLargeTransactionSet(1000);

        final CompletableFuture<List<TransactionDto>> future1 = CompletableFuture.completedFuture(largeCollection1);
        final CompletableFuture<List<TransactionDto>> future2 = CompletableFuture.completedFuture(largeCollection2);

        when(fileService.parseFileAsync(file1)).thenReturn(future1);
        when(fileService.parseFileAsync(file2)).thenReturn(future2);

        final ReconciliationResponse expectedResponse = ReconciliationResponse.builder()
                .totalRecordsInFile1(1000)
                .totalRecordsInFile2(1000)
                .matchedRecords(1000)
                .unmatchedRecordsInFile1(0)
                .unmatchedRecordsInFile2(0)
                .matchPercentage(100.0)
                .build();

        when(comparisonService.compareData(largeCollection1, largeCollection2)).thenReturn(expectedResponse);

        // Act
        final ReconciliationResponse response = transactionService.reconcileTransactions(file1, file2);

        // Assert
        assertNotNull(response);
        assertEquals(1000, response.totalRecordsInFile1());
        assertEquals(1000, response.totalRecordsInFile2());
        assertEquals(1000, response.matchedRecords());
        assertEquals(100.0, response.matchPercentage());

        verify(fileService, times(1)).parseFileAsync(file1);
        verify(fileService, times(1)).parseFileAsync(file2);
        verify(comparisonService, times(1)).compareData(largeCollection1, largeCollection2);
    }

    @Test
    @DisplayName("Should throw FileProcessingException when both files fail to parse")
    void shouldThrowFileProcessingExceptionWhenBothFilesFail() {
        // Arrange
        final RuntimeException cause = new RuntimeException("Both files failed");
        final CompletableFuture<List<TransactionDto>> failedFuture1 = CompletableFuture.failedFuture(cause);
        final CompletableFuture<List<TransactionDto>> failedFuture2 = CompletableFuture.failedFuture(cause);

        when(fileService.parseFileAsync(file1)).thenReturn(failedFuture1);
        when(fileService.parseFileAsync(file2)).thenReturn(failedFuture2);

        // Act & Assert
        final FileProcessingException exception = assertThrows(
                FileProcessingException.class,
                () -> transactionService.reconcileTransactions(file1, file2)
        );

        assertNotNull(exception);
        assertEquals("Failed to process file: File parsing failed", exception.getMessage());

        verify(fileService, times(1)).parseFileAsync(file1);
        verify(fileService, times(1)).parseFileAsync(file2);
        verifyNoInteractions(comparisonService);
    }
}