package luka.mugosa.filecomparison.service.impl;

import luka.mugosa.filecomparison.domain.dto.TransactionDto;
import luka.mugosa.filecomparison.domain.dto.response.ReconciliationResponse;
import luka.mugosa.filecomparison.domain.exception.FileProcessingException;
import luka.mugosa.filecomparison.service.FileService;
import luka.mugosa.filecomparison.service.TransactionService;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
public class TransactionServiceImpl implements TransactionService {

    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(TransactionServiceImpl.class);

    private final ComparisonServiceImpl comparisonService;
    private final FileService fileService;

    public TransactionServiceImpl(ComparisonServiceImpl comparisonService, FileService fileService) {
        this.comparisonService = comparisonService;
        this.fileService = fileService;
    }

    public ReconciliationResponse reconcileTransaction(MultipartFile file1, MultipartFile file2) {
        final CompletableFuture<Set<TransactionDto>> collectionFuture1 = fileService.parseFileAsync(file1);
        final CompletableFuture<Set<TransactionDto>> collectionFuture2 = fileService.parseFileAsync(file2);
        try {
            final CompletableFuture<Void> allOf = CompletableFuture.allOf(collectionFuture1, collectionFuture2);
            allOf.get(2, TimeUnit.MINUTES); // 2-minute timeout

            final Set<TransactionDto> collection1 = collectionFuture1.join();
            final Set<TransactionDto> collection2 = collectionFuture2.join();

            return comparisonService.compareData(collection1, collection2);
        } catch (TimeoutException e) {
            logger.error("File parsing timed out after 2 minutes", e);
            throw new FileProcessingException("File parsing operation timed out", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new FileProcessingException("File parsing was interrupted", e);
        } catch (ExecutionException e) {
            throw new FileProcessingException("File parsing failed", e.getCause());
        }
    }

}
