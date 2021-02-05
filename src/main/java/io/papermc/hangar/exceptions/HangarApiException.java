package io.papermc.hangar.exceptions;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.jackson.JsonComponent;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;

//@JsonSerialize(using = HangarApiException.HangarApiExceptionSerializer.class)
public class HangarApiException extends ResponseStatusException {

    private final HttpHeaders httpHeaders;

    public HangarApiException(HttpStatus status) {
        super(status);
        this.httpHeaders = HttpHeaders.EMPTY;
    }

    public HangarApiException(HttpStatus status, String reason) {
        super(status, reason);
        this.httpHeaders = HttpHeaders.EMPTY;
    }

    public HangarApiException(HttpStatus status, HttpHeaders httpHeaders) {
        super(status);
        this.httpHeaders = httpHeaders;
    }

    public HangarApiException(HttpStatus status, String reason, HttpHeaders httpHeaders) {
        super(status, reason);
        this.httpHeaders = httpHeaders;
    }

    @NotNull
    @Override
    public HttpHeaders getResponseHeaders() {
        return httpHeaders;
    }

    @JsonComponent
    public static class HangarApiExceptionSerializer extends JsonSerializer<HangarApiException> {

        @Override
        public void serialize(HangarApiException exception, JsonGenerator gen, SerializerProvider provider) throws IOException {
            String message = exception.getReason();
            if (message == null || message.isBlank()) {
                message = exception.getStatus().getReasonPhrase();
            }
            gen.writeStartObject();
            gen.writeStringField("message", message);
            gen.writeBooleanField("isHangarApiException", true);
            gen.writeObjectFieldStart("httpError");
            gen.writeNumberField("statusCode", exception.getStatus().value());
            gen.writeStringField("statusPhrase", exception.getStatus().getReasonPhrase());
            gen.writeEndObject();
        }
    }
}
