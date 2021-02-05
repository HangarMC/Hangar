package io.papermc.hangar.model.common.projects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ReviewState {
    UNREVIEWED("unreviewed"),

    REVIEWED("reviewed"),

    BACKLOG("backlog"),

    PARTIALLY_REVIEWED("partially_reviewed");

    private final String apiName;

    ReviewState(String apiName) {
        this.apiName = apiName;
    }

    @Override
    @JsonValue
    public String toString() {
        return apiName;
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
            if (String.valueOf(b.apiName).equals(text)) {
                return b;
            }
        }
        return null;
    }
}
