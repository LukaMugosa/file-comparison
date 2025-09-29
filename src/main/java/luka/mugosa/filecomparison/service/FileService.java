package luka.mugosa.filecomparison.service;

import luka.mugosa.filecomparison.domain.dto.TransactionDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface FileService {
    List<TransactionDto> parseFile(String path);

    List<TransactionDto> parseFile(MultipartFile file);

    CompletableFuture<List<TransactionDto>> parseFileAsync(String path);

    CompletableFuture<List<TransactionDto>> parseFileAsync(MultipartFile file);
}
