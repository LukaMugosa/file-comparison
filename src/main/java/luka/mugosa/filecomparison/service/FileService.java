package luka.mugosa.filecomparison.service;

import luka.mugosa.filecomparison.domain.dto.TransactionDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

public interface FileService {
    Set<TransactionDto> parseFile(String path);

    Set<TransactionDto> parseFile(MultipartFile file);
}
