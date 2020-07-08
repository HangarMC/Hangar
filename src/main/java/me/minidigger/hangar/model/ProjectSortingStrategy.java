package me.minidigger.hangar.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Gets or Sets ProjectSortingStrategy
 */
public enum ProjectSortingStrategy {
    STARS("stars"),
    DOWNLOADS("downloads"),
    VIEWS("views"),
    NEWEST("newest"),
    UPDATED("updated"),
    ONLY_RELEVANCE("only_relevance"),
    RECENT_DOWNLOADS("recent_downloads"),
    RECENT_VIEWS("recent_views");

    private final String value;

    ProjectSortingStrategy(String value) {
        this.value = value;
    }

    @Override
    @JsonValue
    public String toString() {
        return String.valueOf(value);
    }

    @JsonCreator
    public static ProjectSortingStrategy fromValue(String text) {
        for (ProjectSortingStrategy b : ProjectSortingStrategy.values()) {
            if (String.valueOf(b.value).equals(text)) {
                return b;
            }
        }
        return null;
    }
}
