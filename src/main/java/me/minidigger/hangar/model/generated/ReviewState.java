package me.minidigger.hangar.model.generated;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Gets or Sets reviewState
 */
public enum ReviewState {
    UNREVIEWED(0, "unreviewed"),

    REVIEWED(1, "reviewed"),

    BACKLOG(2, "backlog"),

    PARTIALLY_REVIEWED(3, "partially_reviewed");

    private final long value;
    private final String apiName;

    ReviewState(long value, String apiName) {
        this.value = value;
        this.apiName = apiName;
    }

    @Override
    @JsonValue
    public String toString() {
        return String.valueOf(value);
    }

    public long getValue() {
        return value;
    }

    public String getApiName() {
        return apiName;
    }

    public boolean isChecked() {
        return this == ReviewState.REVIEWED || this == ReviewState.PARTIALLY_REVIEWED;
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
