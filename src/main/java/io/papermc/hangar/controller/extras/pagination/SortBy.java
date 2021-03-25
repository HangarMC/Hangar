package io.papermc.hangar.controller.extras.pagination;

public interface SortBy extends QueryIdentified {

    void createSql(StringBuilder sb);
}
