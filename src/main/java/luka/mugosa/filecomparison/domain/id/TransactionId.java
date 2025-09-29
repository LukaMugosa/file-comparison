package luka.mugosa.filecomparison.domain.id;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import luka.mugosa.filecomparison.domain.id.serializer.TransactionIdSerializer;

@JsonSerialize(using = TransactionIdSerializer.class)
public record TransactionId(String id) {
}
