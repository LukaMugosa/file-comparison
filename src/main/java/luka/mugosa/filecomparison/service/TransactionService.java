package luka.mugosa.filecomparison.service;

import luka.mugosa.filecomparison.domain.dto.response.ReconciliationResponse;
import org.springframework.web.multipart.MultipartFile;

public interface TransactionService {
    ReconciliationResponse reconcileTransactions(MultipartFile file1, MultipartFile file2);
}
