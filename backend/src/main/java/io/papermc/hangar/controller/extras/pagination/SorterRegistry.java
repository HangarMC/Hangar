package io.papermc.hangar.controller.extras.pagination;

import io.papermc.hangar.exceptions.HangarApiException;
import java.util.HashMap;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

public enum SorterRegistry implements Sorter {

    USER_JOIN_DATE("createdAt", simpleSorter("u.created_at"), null),
    USER_NAME("name", simpleSorter("LOWER(u.name)"),null ),
    USER_PROJECT_COUNT("projectCount", simpleSorter("project_count"), null),
    USER_ROLES("roles", simpleSorter("roles"), null),
    USER_ORG("org", simpleSorter("is_organization"), null),
    USER_LOCKED("locked", simpleSorter("locked"), null),

    // For Projects
    VIEWS("views", simpleSorter("hp.views"), simpleSorter("stats.views")),
    STARS("stars", simpleSorter("hp.stars"), simpleSorter("stats.stars")),
    DOWNLOADS("downloads", simpleSorter("hp.downloads"), simpleSorter("stats.downloads")),
    NEWEST("newest", simpleSorter("hp.created_at"), simpleSorter("createdAt")),
    UPDATED("updated", simpleSorter("last_updated_double"), simpleSorter("lastUpdated")),
    RECENT_VIEWS("recent_views", simpleSorter("hp.recent_views"), simpleSorter("status.recentViews")),
    RECENT_DOWNLOADS("recent_downloads", simpleSorter("hp.recent_downloads"), simpleSorter("stats.recentDownloads")),
    SLUG("slug", simpleSorter("LOWER(hp.slug)"), simpleSorter("name"));

    private static final Map<String, SorterRegistry> SORTERS = new HashMap<>();

    private final String name;
    private final Sorter dbSorter;
    private final Sorter meiliSorter;

    SorterRegistry(final String name, final Sorter dbSorter, final Sorter meiliSorter) {
        this.name = name;
        this.dbSorter = dbSorter;
        this.meiliSorter = meiliSorter;
    }

    public String getName() {
        return this.name;
    }

    @Override
    public void applySorting(final StringBuilder sb, final SortDirection dir, final PaginationType type) {
        if (type == PaginationType.MEILI) {
            this.meiliSorter.applySorting(sb, dir, type);
        } else if (type == PaginationType.DB) {
            this.dbSorter.applySorting(sb, dir, type);
        } else {
            throw new HangarApiException("Invalid sort type: " + type);
        }
    }

    private static Sorter simpleSorter(final @NotNull String columnName) {
        return (sb, dir, type) -> sb.append(columnName).append(dir.apply(type));
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
        ASCENDING(" ASC", ":asc"),
        DESCENDING(" DESC", ":desc");

        private final String sql;
        private final String meili;

        SortDirection(final String sql, final String meili) {
            this.sql = sql;
            this.meili = meili;
        }

        public String apply(PaginationType type) {
            return switch (type) {
                case DB -> this.sql;
                case MEILI -> this.meili;
            };
        }

        @Override
        public String toString() {
            return this.sql;
        }
    }
}
