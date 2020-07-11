package me.minidigger.hangar.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Gets or Sets ProjectSortingStrategy
 */
public enum ProjectSortingStrategy {

    STARS(0, "Most stars", "p.stars DESC, p.name ASC", "stars"),
    DOWNLOADS(1, "Most downloads", "p.downloads DESC", "downloads"),
    VIEWS(2, "Most views", "p.views DESC", "views"),
    NEWEST(3, "Newest", "p.created_at DESC", "newest"),
    UPDATED(4, "Recently updated", "p.last_updated DESC", "updated"),
    ONLY_RELEVANCE(5, "Only relevance", "p.last_updated DESC", "only_relevance"),
    RECENT_DOWNLOADS(6, "Recent views", "p.recent_views DESC", "recent_views"),
    RECENT_VIEWS(7, "Recent downloads", "p.recent_downloads DESC", "recent_downloads");

    public static final ProjectSortingStrategy Default = UPDATED;

    private final int value;
    private final String title;
    private final String sql;
    private final String apiName;

    ProjectSortingStrategy(int value, String title, String sql, String apiName) {
        this.value = value;
        this.title = title;
        this.sql = sql;
        this.apiName = apiName;
    }

    public int getValue() {
        return value;
    }

    public String getTitle() {
        return title;
    }

    public String getSql() {
        return sql;
    }

    public String getApiName() {
        return apiName;
    }

    @Override
    @JsonValue
    public String toString() {
        return String.valueOf(apiName);
    }

    @JsonCreator
    public static ProjectSortingStrategy fromApiName(String apiName) {
        for (ProjectSortingStrategy b : ProjectSortingStrategy.values()) {
            if (b.apiName.equals(apiName)) {
                return b;
            }
        }
        return null;
    }
}
