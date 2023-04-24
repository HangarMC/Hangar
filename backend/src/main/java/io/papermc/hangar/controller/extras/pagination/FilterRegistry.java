package io.papermc.hangar.controller.extras.pagination;

import io.papermc.hangar.exceptions.HangarApiException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@SuppressWarnings("unchecked")
public class FilterRegistry {

    private final Map<Class<? extends Filter<?, ?>>, Filter<?, ?>> filters = new HashMap<>();

    @Autowired
    public FilterRegistry(final List<? extends Filter<? extends Filter.FilterInstance, ?>> filters) {
        filters.forEach(f -> {
            final Class<? extends Filter<?, ?>> filterClass = (Class<? extends Filter<? extends Filter.FilterInstance, ?>>) f.getClass();
            if (this.filters.containsKey((filterClass))) {
                throw new IllegalArgumentException(filterClass + " is already registered as filter");
            }
        });
        filters.forEach(f -> this.filters.put((Class<? extends Filter<?, ?>>) f.getClass(), f));
    }

    public @NotNull <T extends Filter<? extends Filter.FilterInstance, ?>> T get(final Class<T> filterClass) {
        if (this.filters.containsKey(filterClass)) {
            return (T) this.filters.get(filterClass);
        }
        throw new HangarApiException(filterClass + " is not a registered filter");
    }
}
