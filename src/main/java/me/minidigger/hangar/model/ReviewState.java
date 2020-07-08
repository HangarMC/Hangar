package me.minidigger.hangar.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Gets or Sets reviewState
 */
public enum ReviewState {
    UNREVIEWED("unreviewed"),

    REVIEWED("reviewed"),

    BACKLOG("backlog"),

    PARTIALLY_REVIEWED("partially_reviewed");

    private final String value;

    ReviewState(String value) {
        this.value = value;
    }

    @Override
    @JsonValue
    public String toString() {
        return String.valueOf(value);
    }

    @JsonCreator
    public static ReviewState fromValue(String text) {
        for (ReviewState b : ReviewState.values()) {
            if (String.valueOf(b.value).equals(text)) {
                return b;
            }
        }
        return null;
    }
}
