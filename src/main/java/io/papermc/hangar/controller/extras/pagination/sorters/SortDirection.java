package io.papermc.hangar.controller.extras.pagination.sorters;

public enum SortDirection {
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
