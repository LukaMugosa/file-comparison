package luka.mugosa.filecomparison.service;

import luka.mugosa.filecomparison.domain.dto.TransactionDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

public interface FileService {
    Set<TransactionDto> parseFile(String path);

    Set<TransactionDto> parseFile(MultipartFile file);

    CompletableFuture<Set<TransactionDto>> parseFileAsync(String path);

    CompletableFuture<Set<TransactionDto>> parseFileAsync(MultipartFile file);
}
