package io.papermc.hangar.model.internal.user.notifications;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Locale;

public enum NotificationType {
    NEUTRAL,
    SUCCESS,
    INFO,
    WARNING,
    ERROR;

    @JsonValue
    public String getValue() {
        return this.name().toLowerCase(Locale.ROOT);
    }
}
