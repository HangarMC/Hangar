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

public class HangarApiException extends ResponseStatusException {

    private final HttpHeaders httpHeaders;
    private final Object[] args;

    public HangarApiException() {
        this(HttpStatus.BAD_REQUEST);
    }

    public HangarApiException(final String reason) {
        this(HttpStatus.BAD_REQUEST, reason);
    }

    public HangarApiException(final String reason, final Object... args) {
        this(HttpStatus.BAD_REQUEST, reason, args);
    }

    public HangarApiException(final HttpStatus status) {
        super(status);
        this.httpHeaders = HttpHeaders.EMPTY;
        this.args = new String[0];
    }

    public HangarApiException(final HttpStatus status, final String reason) {
        super(status, reason);
        this.httpHeaders = HttpHeaders.EMPTY;
        this.args = new String[0];
    }

    public HangarApiException(final int status, final String reason) {
        super(status, reason, null);
        this.httpHeaders = HttpHeaders.EMPTY;
        this.args = new String[0];
    }

    public HangarApiException(final HttpStatus status, final HttpHeaders httpHeaders) {
        super(status);
        this.httpHeaders = httpHeaders;
        this.args = new String[0];
    }

    public HangarApiException(final HttpStatus status, final String reason, final HttpHeaders httpHeaders) {
        super(status, reason);
        this.httpHeaders = httpHeaders;
        this.args = new String[0];
    }

    public HangarApiException(final HttpStatus status, final String reason, final Object... args) {
        super(status, reason);
        this.httpHeaders = HttpHeaders.EMPTY;
        this.args = args;
    }

    public static HangarApiException notFound() {
        return new HangarApiException(HttpStatus.NOT_FOUND);
    }

    public static HangarApiException rateLimited() {
        return new HangarApiException(HttpStatus.TOO_MANY_REQUESTS);
    }

    public static HangarApiException forbidden() {
        return new HangarApiException(HttpStatus.FORBIDDEN);
    }

    public static HangarApiException unauthorized(final String msg) {
        return new HangarApiException(HttpStatus.UNAUTHORIZED, msg);
    }

    public Object[] getArgs() {
        return this.args;
    }

    @NotNull
    @Override
    public HttpHeaders getResponseHeaders() {
        return this.httpHeaders;
    }

    @JsonComponent
    public static class HangarApiExceptionSerializer extends JsonSerializer<HangarApiException> {

        @Override
        public void serialize(final HangarApiException exception, final JsonGenerator gen, final SerializerProvider provider) throws IOException {
            String message = exception.getReason();
            HttpStatus status = null;
            try {
                status = exception.getStatus();
            } catch (IllegalArgumentException ignored) {
            }
            if (message == null || message.isBlank()) {
                if (status != null) {
                    message = status.getReasonPhrase();
                } else {
                    message = "UNKNOWN";
                }
            }
            gen.writeStartObject();
            gen.writeStringField("message", message);
            gen.writeArrayFieldStart("messageArgs");
            for (Object arg : exception.args) {
                provider.defaultSerializeValue(arg, gen);
            }
            gen.writeEndArray();
            gen.writeBooleanField("isHangarApiException", true);
            gen.writeObjectFieldStart("httpError");
            if (status != null) {
                gen.writeNumberField("statusCode", status.value());
                gen.writeStringField("statusPhrase", status.getReasonPhrase());
            } else {
                gen.writeNumberField("statusCode", exception.getRawStatusCode());
                gen.writeStringField("statusPhrase", "UNKNOWN");
            }
            gen.writeEndObject();
        }
    }
}
