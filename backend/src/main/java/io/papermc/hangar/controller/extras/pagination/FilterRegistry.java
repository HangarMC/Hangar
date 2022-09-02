package io.papermc.hangar.controller.extras.pagination;

import io.papermc.hangar.controller.extras.pagination.Filter.FilterInstance;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@SuppressWarnings("unchecked")
public class FilterRegistry {

    private final Map<Class<? extends Filter<?>>, Filter<?>> filters = new HashMap<>();

    @Autowired
    public FilterRegistry(List<? extends Filter<? extends FilterInstance>> filters) {
        filters.forEach(f -> {
            var filterClass = (Class<? extends Filter<? extends FilterInstance>>) f.getClass();
            if (this.filters.containsKey((filterClass))) {
                throw new IllegalArgumentException(filterClass + " is already registered as filter");
            }
        });
        filters.forEach(f -> this.filters.put((Class<? extends Filter<?>>) f.getClass(), f));
    }

    @NotNull
    public <T extends Filter<? extends FilterInstance>> T get(Class<T> filterClass) {
        if (filters.containsKey(filterClass)) {
            return (T) filters.get(filterClass);
        }
        throw new IllegalArgumentException(filterClass + " is not a registered filter");
    }
}
