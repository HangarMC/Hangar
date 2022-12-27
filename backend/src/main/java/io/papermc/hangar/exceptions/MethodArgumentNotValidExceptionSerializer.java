package io.papermc.hangar.exceptions;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;
import java.util.Objects;
import org.springframework.boot.jackson.JsonComponent;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;

@JsonComponent
public class MethodArgumentNotValidExceptionSerializer extends JsonSerializer<MethodArgumentNotValidException> {

    @Override
    public void serialize(final MethodArgumentNotValidException exception, final JsonGenerator gen, final SerializerProvider provider) throws IOException {
        gen.writeStartObject();
        gen.writeStringField("message", exception.getMessage());
        gen.writeStringField("object", exception.getObjectName());
        gen.writeBooleanField("isHangarValidationException", true);
        gen.writeObjectFieldStart("httpError");
        gen.writeNumberField("statusCode", HttpStatus.BAD_REQUEST.value());
        gen.writeStringField("statusPhrase", HttpStatus.BAD_REQUEST.getReasonPhrase());
        gen.writeEndObject();
        gen.writeArrayFieldStart("globalErrors");
        for (final ObjectError globalError : exception.getGlobalErrors()) {
            gen.writeStartObject();
            gen.writeStringField("code", globalError.getCode());
            gen.writeStringField("errorMsg", globalError.getDefaultMessage());
            gen.writeObjectField("objectName", globalError.getObjectName());
            gen.writeEndObject();
        }
        gen.writeEndArray();
        gen.writeArrayFieldStart("fieldErrors");
        for (final FieldError fieldError : exception.getFieldErrors()) {
            gen.writeStartObject();
            gen.writeStringField("code", fieldError.getCode());
            gen.writeStringField("errorMsg", fieldError.getDefaultMessage());
            gen.writeStringField("fieldName", fieldError.getField());
            gen.writeStringField("rejectedValue", Objects.toString(fieldError.getRejectedValue()));
            gen.writeEndObject();
        }
        gen.writeEndArray();
    }
}
