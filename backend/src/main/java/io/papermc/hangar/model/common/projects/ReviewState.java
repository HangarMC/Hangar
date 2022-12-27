package io.papermc.hangar.model.common.projects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ReviewState {
    UNREVIEWED("unreviewed", "Unreviewed"),

    REVIEWED("reviewed", "Reviewed"),

    UNDER_REVIEW("under_review", "Under Review"),

    PARTIALLY_REVIEWED("partially_reviewed", "Partially Reviewed");

    private final String apiName;
    private final String frontendName;

    ReviewState(final String apiName, final String frontendName) {
        this.apiName = apiName;
        this.frontendName = frontendName;
    }

    @Override
    @JsonValue
    public String toString() {
        return this.apiName;
    }

    public String getApiName() {
        return this.apiName;
    }

    public String getFrontendName() {
        return this.frontendName;
    }

    public boolean isChecked() {
        return this == REVIEWED || this == PARTIALLY_REVIEWED;
    }

    @JsonCreator
    public static ReviewState fromValue(final String text) {
        for (final ReviewState b : values()) {
            if (String.valueOf(b.apiName).equals(text)) {
                return b;
            }
        }
        return null;
    }

    private static final ReviewState[] VALUES = values();

    public static ReviewState[] getValues() {
        return VALUES;
    }
}
