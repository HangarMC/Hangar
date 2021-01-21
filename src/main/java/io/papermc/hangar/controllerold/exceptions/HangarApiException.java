package io.papermc.hangar.controllerold.exceptions;

import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

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
}
