package luka.mugosa.filecomparison.rest.v1;

import luka.mugosa.filecomparison.domain.dto.response.ReconciliationResponse;
import luka.mugosa.filecomparison.service.impl.TransactionServiceImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1")
public class TransactionResource {

    private final TransactionServiceImpl transactionService;

    public TransactionResource(TransactionServiceImpl transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping("/reconcile-transactions")
    public ResponseEntity<ReconciliationResponse> compareFiles(
            @RequestParam("file1") MultipartFile file1,
            @RequestParam("file2") MultipartFile file2) {

        final ReconciliationResponse reconciliationResponse = transactionService.reconcileTransaction(file1, file2);

        return ResponseEntity.ok(reconciliationResponse);
    }
}
