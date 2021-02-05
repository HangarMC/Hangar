package io.papermc.hangar.exceptions.handlers;

import io.papermc.hangar.exceptions.HangarApiException;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice(basePackages = "io.papermc.hangar.controller")
public class HangarEntityExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(HangarApiException.class)
    public ResponseEntity<HangarApiException> handleException(HangarApiException exception) {
        return new ResponseEntity<>(exception, exception.getResponseHeaders(), exception.getStatus());
    }

    @NotNull
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(@NotNull MethodArgumentNotValidException ex, @NotNull HttpHeaders headers, @NotNull HttpStatus status, @NotNull WebRequest request) {
        return new ResponseEntity<>(ex, headers, status);
    }
}
