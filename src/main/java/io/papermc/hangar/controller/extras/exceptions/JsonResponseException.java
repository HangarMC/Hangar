package io.papermc.hangar.controller.extras.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;

public class JsonResponseException extends ResponseStatusException {

    private final String messageKey;
    private final String[] args;
    public JsonResponseException(String messageKey, Object...args) {
        super(HttpStatus.BAD_REQUEST);
        this.messageKey = messageKey;
        this.args = Arrays.stream(args).map(Object::toString).toArray(String[]::new);
    }

    public String getMessageKey() {
        return messageKey;
    }

    public String[] getArgs() {
        return args;
    }
}
