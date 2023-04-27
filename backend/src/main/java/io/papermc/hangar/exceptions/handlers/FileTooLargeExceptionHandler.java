package io.papermc.hangar.exceptions.handlers;

import io.papermc.hangar.config.hangar.HangarConfig;
import io.papermc.hangar.exceptions.HangarApiException;
import io.undertow.server.RequestTooBigException;
import io.undertow.server.handlers.form.MultiPartParserDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
@Order(1)
public class FileTooLargeExceptionHandler extends ResponseEntityExceptionHandler {
    private final HangarConfig config;

    @Autowired
    public FileTooLargeExceptionHandler(final HangarConfig config) {
        this.config = config;
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
