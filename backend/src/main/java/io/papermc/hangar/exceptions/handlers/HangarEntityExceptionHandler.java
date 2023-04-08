package io.papermc.hangar.exceptions.handlers;

import io.papermc.hangar.config.hangar.HangarConfig;
import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.exceptions.HangarResponseException;
import io.papermc.hangar.exceptions.MultiHangarApiException;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice(basePackages = "io.papermc.hangar")
public class HangarEntityExceptionHandler extends ResponseEntityExceptionHandler {

    private final HangarConfig config;

    @Autowired
    public HangarEntityExceptionHandler(final HangarConfig config) {
        this.config = config;
    }

    @ExceptionHandler(HangarApiException.class)
    public ResponseEntity<HangarApiException> handleException(final HangarApiException exception) {
        return new ResponseEntity<>(exception, exception.getHeaders(), exception.getStatusCode().value());
    }

    @ExceptionHandler(MultiHangarApiException.class)
    public ResponseEntity<MultiHangarApiException> handleException(final MultiHangarApiException exception) {
        return new ResponseEntity<>(exception, exception.getHeaders(), exception.getStatusCode().value());
    }

    @ExceptionHandler(HangarResponseException.class)
    public ResponseEntity<Object> handleException(final HangarResponseException exception) {
        return ResponseEntity.status(exception.getStatus()).headers(exception.getHeaders()).body(exception);
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(final Exception ex, final Object body, final HttpHeaders headers, final HttpStatusCode status, final WebRequest request) {
        if (this.config.isDev()) {
            return new ResponseEntity<>(new HangarApiException(ex.getMessage()), status);
        } else {
            return super.handleExceptionInternal(ex, body, headers, status, request);
        }
    }

    @Override
    protected @NotNull ResponseEntity<Object> handleMethodArgumentNotValid(final @NotNull MethodArgumentNotValidException ex, final @NotNull HttpHeaders headers, final @NotNull HttpStatusCode status, final @NotNull WebRequest request) {
        return new ResponseEntity<>(ex, headers, status);
    }
}
