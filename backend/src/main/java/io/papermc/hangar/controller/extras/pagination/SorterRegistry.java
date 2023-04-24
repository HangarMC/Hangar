package io.papermc.hangar.controller.extras.pagination;

import io.papermc.hangar.exceptions.HangarApiException;
import java.util.HashMap;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

public enum SorterRegistry implements Sorter {

    USER_JOIN_DATE("createdAt", simpleSorter("u.created_at")),
    USER_NAME("name", simpleSorter("LOWER(u.name)")),
    USER_PROJECT_COUNT("projectCount", simpleSorter("project_count")),
    USER_ROLES("roles", simpleSorter("roles")),
    USER_ORG("org", simpleSorter("is_organization")),
    USER_LOCKED("locked", simpleSorter("locked")),

    // For Projects
    VIEWS("views", simpleSorter("hp.views")),
    STARS("stars", simpleSorter("hp.stars")),
    DOWNLOADS("downloads", simpleSorter("hp.downloads")),
    NEWEST("newest", simpleSorter("p.created_at")),
    UPDATED("updated", simpleSorter("last_updated_double")),
    RECENT_VIEWS("recent_views", simpleSorter("hp.recent_views")),
    RECENT_DOWNLOADS("recent_downloads", simpleSorter("hp.recent_downloads")),
    SLUG("slug", simpleSorter("LOWER(p.slug)"));

    private static final Map<String, SorterRegistry> SORTERS = new HashMap<>();

    private final String name;
    private final Sorter sorter;

    SorterRegistry(final String name, final Sorter sorter) {
        this.name = name;
        this.sorter = sorter;
    }

    public String getName() {
        return this.name;
    }

    @Override
    public void applySorting(final StringBuilder sb, final SortDirection dir) {
        this.sorter.applySorting(sb, dir);
    }

    private static Sorter simpleSorter(final @NotNull String columnName) {
        return (sb, dir) -> sb.append(columnName).append(dir);
    }

    public static @NotNull SorterRegistry getSorter(final @NotNull String name) {
        if (SORTERS.isEmpty()) {
            for (final SorterRegistry value : values()) {
                SORTERS.put(value.name, value);
            }
        }
        if (SORTERS.containsKey(name)) {
            return SORTERS.get(name);
        }
        throw new HangarApiException(name + " is not a registered sorter");
    }

    public enum SortDirection {
        ASCENDING(" ASC"),
        DESCENDING(" DESC");

        private final String sql;

        SortDirection(final String sql) {
            this.sql = sql;
        }

        @Override
        public String toString() {
            return this.sql;
        }
    }
}
