package me.minidigger.hangar.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Gets or Sets visibility
 */
public enum Visibility {
    PUBLIC("public"),

    NEW("new"),

    NEEDSCHANGES("needsChanges"),

    NEEDSAPPROVAL("needsApproval"),

    SOFTDELETE("softDelete");

    private final String value;

    Visibility(String value) {
        this.value = value;
    }

    @Override
    @JsonValue
    public String toString() {
        return String.valueOf(value);
    }

    @JsonCreator
    public static Visibility fromValue(String text) {
        for (Visibility b : Visibility.values()) {
            if (String.valueOf(b.value).equals(text)) {
                return b;
            }
        }
        return null;
    }
}
