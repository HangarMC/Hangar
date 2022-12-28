package io.papermc.hangar.exceptions;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;
import java.util.List;
import org.springframework.boot.jackson.JsonComponent;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class MultiHangarApiException extends ResponseStatusException {

    private final List<HangarApiException> exceptions;

    public MultiHangarApiException(final List<HangarApiException> exceptions) {
        super(HttpStatus.BAD_REQUEST);
        this.exceptions = exceptions;
    }

    public List<HangarApiException> getExceptions() {
        return this.exceptions;
    }

    @JsonComponent
    public static class MultiExceptionSerializer extends JsonSerializer<MultiHangarApiException> {

        @Override
        public void serialize(final MultiHangarApiException value, final JsonGenerator gen, final SerializerProvider serializers) throws IOException {
            gen.writeStartObject();
            gen.writeBooleanField("isMultiException", true);
            gen.writeBooleanField("isHangarApiException", true);
            gen.writeArrayFieldStart("exceptions");
            for (final HangarApiException exception : value.exceptions) {
                final String message = exception.getReason();
                gen.writeStartObject();
                gen.writeStringField("message", message);
                gen.writeArrayFieldStart("messageArgs");
                for (final Object arg : exception.getArgs()) {
                    serializers.defaultSerializeValue(arg, gen);
                }
                gen.writeEndArray();
                gen.writeBooleanField("isHangarApiException", true);
                gen.writeObjectFieldStart("httpError");
                gen.writeNumberField("statusCode", exception.getStatusCode().value());
                gen.writeEndObject();
                gen.writeEndObject();
            }
            gen.writeEndArray();
            gen.writeEndObject();
        }
    }
}
