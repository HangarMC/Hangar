package io.papermc.hangar.model.internal.api.requests.versions;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.node.ObjectNode;
import javax.validation.constraints.NotNull;

public class ReviewMessage {

    private final @NotNull String message;
    private final @NotNull ObjectNode args;

    @JsonCreator
    public ReviewMessage(final String message, final ObjectNode args) {
        this.message = message;
        this.args = args;
    }

    public String getMessage() {
        return this.message;
    }

    public ObjectNode getArgs() {
        return this.args;
    }
}
