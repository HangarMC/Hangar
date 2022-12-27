package io.papermc.hangar.exceptions;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.springframework.boot.jackson.JsonComponent;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;

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
                String message = exception.getReason();
                if (message == null || message.isBlank()) {
                    message = exception.getStatus().getReasonPhrase();
                }
                gen.writeStartObject();
                gen.writeStringField("message", message);
                gen.writeArrayFieldStart("messageArgs");
                for (final Object arg : exception.getArgs()) {
                    serializers.defaultSerializeValue(arg, gen);
                }
                gen.writeEndArray();
                gen.writeBooleanField("isHangarApiException", true);
                gen.writeObjectFieldStart("httpError");
                gen.writeNumberField("statusCode", exception.getStatus().value());
                gen.writeStringField("statusPhrase", exception.getStatus().getReasonPhrase());
                gen.writeEndObject();
                gen.writeEndObject();
            }
            gen.writeEndArray();
            gen.writeEndObject();
        }
    }
}
