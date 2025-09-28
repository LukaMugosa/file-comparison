package luka.mugosa.filecomparison.service;

import luka.mugosa.filecomparison.domain.dto.TransactionDto;
import luka.mugosa.filecomparison.domain.dto.response.ReconciliationResponse;

import java.util.Collection;

public interface ComparisonService {
    ReconciliationResponse compareData(Collection<TransactionDto> collection1, Collection<TransactionDto> collection2);

}
