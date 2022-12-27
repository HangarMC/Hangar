package io.papermc.hangar.exceptions;

import java.util.List;
import java.util.Map;

public final class WebHookException extends RuntimeException {

    private final Map<String, Object> error;

    private WebHookException(final Map<String, Object> error) {
        this.error = error;
    }

    public Map<String, Object> getError() {
        return this.error;
    }

    public static WebHookException of(final String message) {
        return new WebHookException(Map.of("messages", List.of(Map.of(
            "instance_ptr", "#/method",
            "messages", List.of(
                Map.of("id", 123,
                    "text", message,
                    "type", "error")
            )))));
    }
}
