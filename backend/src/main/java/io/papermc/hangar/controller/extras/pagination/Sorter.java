package io.papermc.hangar.controller.extras.pagination;

import java.util.function.Consumer;

@FunctionalInterface
public interface Sorter {

    void applySorting(StringBuilder sb, SorterRegistry.SortDirection dir, PaginationType type);

    default Consumer<StringBuilder> ascending(final PaginationType type) {
        return sb -> this.applySorting(sb, SorterRegistry.SortDirection.ASCENDING, type);
    }

    default Consumer<StringBuilder> descending(final PaginationType type) {
        return sb -> this.applySorting(sb, SorterRegistry.SortDirection.DESCENDING, type);
    }
}
