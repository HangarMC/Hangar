package io.papermc.hangar.model.api.project;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.jetbrains.annotations.Nullable;

/**
 * Gets or Sets ProjectSortingStrategy
 */
public enum ProjectSortingStrategy {

    STARS("Most stars", "hp.stars DESC, p.name ASC", "stars"),
    DOWNLOADS("Most downloads", "hp.downloads DESC", "downloads"),
    VIEWS("Most views", "hp.views DESC", "views"),
    NEWEST("Newest", "p.created_at DESC", "newest"),
    UPDATED("Recently updated", "hp.last_updated DESC", "updated"),
    RECENT_DOWNLOADS("Recent views", "hp.recent_views DESC", "recent_views"),
    RECENT_VIEWS("Recent downloads", "hp.recent_downloads DESC", "recent_downloads");

    private static final ProjectSortingStrategy[] VALUES = values();
    private final String title;
    private final String sql;
    private final String apiName;

    ProjectSortingStrategy(final String title, final String sql, final String apiName) {
        this.title = title;
        this.sql = sql;
        this.apiName = apiName;
    }

    public String getTitle() {
        return this.title;
    }

    public String getSql() {
        return this.sql;
    }

    public String getApiName() {
        return this.apiName;
    }

    @Override
    @JsonValue
    public String toString() {
        return String.valueOf(this.apiName);
    }

    @JsonCreator
    public static @Nullable ProjectSortingStrategy fromApiName(final String apiName) {
        for (final ProjectSortingStrategy strategy : VALUES) {
            if (strategy.apiName.equals(apiName)) {
                return strategy;
            }
        }
        return null;
    }
}
