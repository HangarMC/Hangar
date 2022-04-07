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

    public HangarApiException(String reason) {
        this(HttpStatus.BAD_REQUEST, reason);
    }

    public HangarApiException(String reason, Object...args) {
        this(HttpStatus.BAD_REQUEST, reason, args);
    }

    public HangarApiException(HttpStatus status) {
        super(status);
        this.httpHeaders = HttpHeaders.EMPTY;
        this.args = new String[0];
    }

    public HangarApiException(HttpStatus status, String reason) {
        super(status, reason);
        this.httpHeaders = HttpHeaders.EMPTY;
        this.args = new String[0];
    }

    public HangarApiException(HttpStatus status, HttpHeaders httpHeaders) {
        super(status);
        this.httpHeaders = httpHeaders;
        this.args = new String[0];
    }

    public HangarApiException(HttpStatus status, String reason, HttpHeaders httpHeaders) {
        super(status, reason);
        this.httpHeaders = httpHeaders;
        this.args = new String[0];
    }

    public HangarApiException(HttpStatus status, String reason, Object...args) {
        super(status, reason);
        this.httpHeaders = HttpHeaders.EMPTY;
        this.args = args;
    }

    public Object[] getArgs() {
        return args;
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
            gen.writeArrayFieldStart("messageArgs");
            for (Object arg : exception.args) {
                provider.defaultSerializeValue(arg, gen);
            }
            gen.writeEndArray();
            gen.writeBooleanField("isHangarApiException", true);
            gen.writeObjectFieldStart("httpError");
            gen.writeNumberField("statusCode", exception.getStatus().value());
            gen.writeStringField("statusPhrase", exception.getStatus().getReasonPhrase());
            gen.writeEndObject();
        }
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

    public static HangarApiException unauthorized(String msg) {
        return new HangarApiException(HttpStatus.UNAUTHORIZED, msg);
    }
}
