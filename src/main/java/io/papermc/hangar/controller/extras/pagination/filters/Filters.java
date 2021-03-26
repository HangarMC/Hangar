package io.papermc.hangar.controller.extras.pagination.filters;

import io.papermc.hangar.controller.extras.pagination.filters.Filter.FilterInstance;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unchecked")
public class Filters {

    private static final Map<Class<? extends Filter<?>>, Filter<?>> ALL_FILTERS = new HashMap<>();

    public static final ProjectCategoryFilter PROJECT_CATEGORY_FILTER = register(new ProjectCategoryFilter());

    private static <F extends Filter<?>> F register(F filter) {
        ALL_FILTERS.put((Class<? extends Filter<?>>) filter.getClass(), filter);
        return filter;
    }

    public static final Class<? extends Filter<? extends FilterInstance>>[] TEST = new Class[] { ProjectCategoryFilter.class };

    @NotNull
    public static <T extends Filter<? extends FilterInstance>> T getFilter(Class<T> filterClass) {
        if (ALL_FILTERS.containsKey(filterClass)) {
            return (T) ALL_FILTERS.get(filterClass);
        }
        throw new IllegalArgumentException(filterClass + " is not a registered filter");
    }
}
