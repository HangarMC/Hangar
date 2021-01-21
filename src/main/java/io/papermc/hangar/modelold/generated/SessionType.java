package io.papermc.hangar.modelold.generated;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Gets or Sets type
 */
public enum SessionType {
    KEY("key"),

    USER("user"),

    PUBLIC("public"),

    DEV("dev");

    private final String value;

    SessionType(String value) {
        this.value = value;
    }

    @Override
    @JsonValue
    public String toString() {
        return String.valueOf(value);
    }

    @JsonCreator
    public static SessionType fromValue(String text) {
        for (SessionType b : SessionType.values()) {
            if (String.valueOf(b.value).equals(text)) {
                return b;
            }
        }
        return null;
    }
}
