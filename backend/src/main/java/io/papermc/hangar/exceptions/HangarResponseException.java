package io.papermc.hangar.exceptions;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;
import org.jetbrains.annotations.Nullable;
import org.springframework.boot.jackson.JsonComponent;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;

public class HangarResponseException extends RuntimeException {

    private final HttpStatusCode status;
    private final String message;
    private final Object body;
    private final @Nullable HttpHeaders headers;

    public HangarResponseException(final HttpStatusCode status, final String message, final Object body) {
        this(status, message, body, null);
    }

    public HangarResponseException(final HttpStatusCode status, final String message, final Object body, final @Nullable HttpHeaders headers) {
        this.status = status;
        this.message = message;
        this.body = body;
        this.headers = headers;
    }

    public Object getBody() {
        return this.body;
    }

    public HttpStatusCode getStatus() {
        return this.status;
    }

    public HttpHeaders getHeaders() {
        return this.headers;
    }

    @Override
    public String getMessage() {
        return this.message;
    }

    @JsonComponent
    public static class HangarResponseExceptionSerializer extends JsonSerializer<HangarResponseException> {

        @Override
        public void serialize(final HangarResponseException exception, final JsonGenerator gen, final SerializerProvider provider) throws IOException {
            final String message = exception.getMessage();
            gen.writeStartObject();
            gen.writeStringField("message", message);
            gen.writeBooleanField("isHangarApiException", true);
            gen.writeObjectField("body", exception.getBody());
            gen.writeObjectFieldStart("httpError");
            gen.writeNumberField("statusCode", exception.getStatus().value());
            gen.writeEndObject();
        }
    }
}
