package io.papermc.hangar.exceptions.handlers;

import io.papermc.hangar.config.hangar.HangarConfig;
import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.exceptions.HangarResponseException;
import io.papermc.hangar.exceptions.MultiHangarApiException;
import io.undertow.server.RequestTooBigException;
import io.undertow.server.handlers.form.MultiPartParserDefinition;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice(basePackages = "io.papermc.hangar")
public class HangarExceptionHandler extends ResponseEntityExceptionHandler {

    private final HangarConfig config;

    @Autowired
    public HangarExceptionHandler(final HangarConfig config) {
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

    @ExceptionHandler(MultiPartParserDefinition.FileTooLargeException.class)
    public ResponseEntity<HangarApiException> handleException(final MultiPartParserDefinition.FileTooLargeException exception) {
        final HangarApiException apiException = new HangarApiException(HttpStatus.PAYLOAD_TOO_LARGE, "File too large - files have to be less than " + this.config.projects.maxFileSizeMB() + "MB in size");
        return new ResponseEntity<>(apiException, HttpStatus.PAYLOAD_TOO_LARGE);
    }

    @ExceptionHandler(RequestTooBigException.class)
    public ResponseEntity<HangarApiException> handleException(final RequestTooBigException exception) {
        final HangarApiException apiException = new HangarApiException(HttpStatus.PAYLOAD_TOO_LARGE, "File too large - files have to be less than " + this.config.projects.maxTotalFilesSizeMB() + "MB total");
        return new ResponseEntity<>(apiException, HttpStatus.PAYLOAD_TOO_LARGE);
    }
}
