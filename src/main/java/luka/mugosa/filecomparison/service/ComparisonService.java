package luka.mugosa.filecomparison.service;

import luka.mugosa.filecomparison.domain.dto.TransactionDto;
import luka.mugosa.filecomparison.domain.dto.response.ReconciliationResponse;

import java.util.List;

public interface ComparisonService {
    ReconciliationResponse compareData(List<TransactionDto> collection1, List<TransactionDto> collection2);
}
