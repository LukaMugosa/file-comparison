package luka.mugosa.filecomparison.domain.id.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import luka.mugosa.filecomparison.domain.id.TransactionId;

import java.io.IOException;

public class TransactionIdSerializer extends JsonSerializer<TransactionId> {

    @Override
    public void serialize(TransactionId value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeString(value.id());
    }
}