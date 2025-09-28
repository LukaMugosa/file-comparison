package luka.mugosa.filecomparison.service.impl;

import luka.mugosa.filecomparison.domain.dto.TransactionDto;
import luka.mugosa.filecomparison.service.FileService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

@Service
public class TransactionServiceImpl {

    private final ComparisonService comparisonService;
    private final FileService fileService;

    public TransactionServiceImpl(ComparisonService comparisonService, FileService fileService) {
        this.comparisonService = comparisonService;
        this.fileService = fileService;
    }

    public void reconcileTransaction(MultipartFile file1, MultipartFile file2) {
        final Set<TransactionDto> transactionsFromFirstFile = fileService.parseFile(file1);
        final Set<TransactionDto> transactionsFromSecondFile = fileService.parseFile(file2);

        comparisonService.compareData(transactionsFromFirstFile, transactionsFromSecondFile);
    }

}
