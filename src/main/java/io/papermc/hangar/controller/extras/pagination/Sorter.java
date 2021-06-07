package io.papermc.hangar.controller.extras.pagination;

import io.papermc.hangar.controller.extras.pagination.SorterRegistry.SortDirection;

import java.util.function.Consumer;

@FunctionalInterface
public interface Sorter {

    void applySorting(StringBuilder sb, SortDirection dir);

    default Consumer<StringBuilder> ascending() {
        return sb -> applySorting(sb, SortDirection.ASCENDING);
    }

    default Consumer<StringBuilder> descending() {
        return sb -> applySorting(sb, SortDirection.DESCENDING);
    }
}
