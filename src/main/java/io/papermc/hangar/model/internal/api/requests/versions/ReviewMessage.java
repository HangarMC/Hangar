package io.papermc.hangar.model.internal.api.requests.versions;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.node.ObjectNode;

import javax.validation.constraints.NotNull;

public class ReviewMessage {

    @NotNull
    private final String message;
    @NotNull
    private final ObjectNode args;

    @JsonCreator
    public ReviewMessage(String message, ObjectNode args) {
        this.message = message;
        this.args = args;
    }

    public String getMessage() {
        return message;
    }

    public ObjectNode getArgs() {
        return args;
    }
}
