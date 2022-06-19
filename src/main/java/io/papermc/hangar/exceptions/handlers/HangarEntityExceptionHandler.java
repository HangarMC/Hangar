package io.papermc.hangar.exceptions.handlers;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import io.papermc.hangar.config.hangar.HangarConfig;
import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.exceptions.MultiHangarApiException;

@ControllerAdvice(basePackages = "io.papermc.hangar.controller")
public class HangarEntityExceptionHandler extends ResponseEntityExceptionHandler {

    private final HangarConfig config;

    @Autowired
    public HangarEntityExceptionHandler(HangarConfig config) {
        this.config = config;
    }

    @ExceptionHandler(HangarApiException.class)
    public ResponseEntity<HangarApiException> handleException(HangarApiException exception) {
        return new ResponseEntity<>(exception, exception.getResponseHeaders(), exception.getStatus());
    }

    @ExceptionHandler(MultiHangarApiException.class)
    public ResponseEntity<MultiHangarApiException> handleException(MultiHangarApiException exception) {
        return new ResponseEntity<>(exception, exception.getResponseHeaders(), exception.getStatus());
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers, HttpStatus status, WebRequest request) {
        if (config.isDev()) {
            return new ResponseEntity<>(new HangarApiException(ex.getMessage()), status);
        } else {
            return super.handleExceptionInternal(ex, body, headers, status, request);
        }
    }

    @NotNull
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(@NotNull MethodArgumentNotValidException ex, @NotNull HttpHeaders headers, @NotNull HttpStatus status, @NotNull WebRequest request) {
        return new ResponseEntity<>(ex, headers, status);
    }
}
