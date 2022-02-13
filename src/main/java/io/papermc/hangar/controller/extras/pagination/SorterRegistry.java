package io.papermc.hangar.controller.extras.pagination;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public enum SorterRegistry implements Sorter {

    USER_JOIN_DATE("joinDate", simpleSorter("u.join_date")),
    USER_NAME("username", simpleSorter("username")),
    USER_PROJECT_COUNT("projectCount", simpleSorter("project_count")),

    //For Projects
    VIEWS("views", simpleSorter("views")),
    STARS("stars", simpleSorter("stars")),
    DOWNLOADS("downloads", simpleSorter("downloads")),
    NEWEST("newest", simpleSorter("created_at")),
    UPDATED("updated", simpleSorter("last_updated"));




    private static final Map<String, SorterRegistry> SORTERS = new HashMap<>();

    private final String name;
    private final Sorter sorter;

    SorterRegistry(String name, Sorter sorter) {
        this.name = name;
        this.sorter = sorter;
    }

    public String getName() {
        return name;
    }

    @Override
    public void applySorting(StringBuilder sb, SortDirection dir) {
        this.sorter.applySorting(sb, dir);
    }

    private static Sorter simpleSorter(@NotNull String columnName) {
        return (sb, dir) -> sb.append(columnName).append(dir);
    }

    @NotNull
    public static SorterRegistry getSorter(@NotNull String name) {
        if (SORTERS.isEmpty()) {
            for (SorterRegistry value : SorterRegistry.values()) {
                SORTERS.put(value.name, value);
            }
        }
        if (SORTERS.containsKey(name)) {
            return SORTERS.get(name);
        }
        throw new IllegalArgumentException(name + " is not a registered sorter");
    }

    enum SortDirection {
        ASCENDING(" ASC"),
        DESCENDING(" DESC");

        private final String sql;

        SortDirection(String sql) {
            this.sql = sql;
        }

        @Override
        public String toString() {
            return sql;
        }
    }
}
