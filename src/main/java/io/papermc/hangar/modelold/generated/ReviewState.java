package io.papermc.hangar.modelold.generated;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;

/**
 * Gets or Sets reviewState
 */
@JsonFormat(shape = Shape.OBJECT)
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
    public String toString() {
        return apiName;
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
