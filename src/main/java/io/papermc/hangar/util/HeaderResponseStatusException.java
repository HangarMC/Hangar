package io.papermc.hangar.util;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class HeaderResponseStatusException extends ResponseStatusException {

    private final HttpHeaders headers;

    public HeaderResponseStatusException(HttpStatus status, HttpHeaders headers) {
        super(status);
        this.headers = headers;
    }

    public HeaderResponseStatusException(HttpStatus status, String reason, HttpHeaders headers) {
        super(status, reason);
        this.headers = headers;
    }

    @Override
    public HttpHeaders getResponseHeaders() {
        return headers;
    }
}
