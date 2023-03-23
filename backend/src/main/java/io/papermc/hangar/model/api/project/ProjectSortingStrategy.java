package io.papermc.hangar.model.api.project;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Gets or Sets ProjectSortingStrategy
 */
public enum ProjectSortingStrategy {

    STARS(0, "Most stars", "hp.stars DESC, p.name ASC", "stars"),
    DOWNLOADS(1, "Most downloads", "hp.downloads DESC", "downloads"),
    VIEWS(2, "Most views", "hp.views DESC", "views"),
    NEWEST(3, "Newest", "p.created_at DESC", "newest"),
    UPDATED(4, "Recently updated", "hp.last_updated DESC", "updated"),
    ONLY_RELEVANCE(5, "Only relevance", "hp.last_updated DESC", "only_relevance"),
    RECENT_DOWNLOADS(6, "Recent views", "hp.recent_views DESC", "recent_views"),
    RECENT_VIEWS(7, "Recent downloads", "hp.recent_downloads DESC", "recent_downloads");

    public static final ProjectSortingStrategy Default = UPDATED;

    private final int value;
    private final String title;
    private final String sql;
    private final String apiName;

    public static final ProjectSortingStrategy[] VALUES = values();

    ProjectSortingStrategy(final int value, final String title, final String sql, final String apiName) {
        this.value = value;
        this.title = title;
        this.sql = sql;
        this.apiName = apiName;
    }

    public int getValue() {
        return this.value;
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
    public static ProjectSortingStrategy fromApiName(final String apiName) {
        for (final ProjectSortingStrategy b : values()) {
            if (b.apiName.equals(apiName)) {
                return b;
            }
        }
        return null;
    }
}
