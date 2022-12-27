package io.papermc.hangar.controller.extras.pagination;

import java.util.function.Consumer;

@FunctionalInterface
public interface Sorter {

    void applySorting(StringBuilder sb, SorterRegistry.SortDirection dir);

    default Consumer<StringBuilder> ascending() {
        return sb -> this.applySorting(sb, SorterRegistry.SortDirection.ASCENDING);
    }

    default Consumer<StringBuilder> descending() {
        return sb -> this.applySorting(sb, SorterRegistry.SortDirection.DESCENDING);
    }
}
