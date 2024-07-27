package io.papermc.hangar.components.observability;

import io.sentry.ISpan;
import io.sentry.Sentry;
import io.sentry.util.StringUtils;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;
import org.springframework.cache.Cache;

public class SentryCacheWrapper implements Cache {

    private final Cache delegate;

    public SentryCacheWrapper(Cache delegate) {
        this.delegate = delegate;
    }

    @Override
    public @NotNull String getName() {
        return this.delegate.getName();
    }

    @Override
    public @NotNull Object getNativeCache() {
        return this.delegate.getNativeCache();
    }

    private <T> T trace(@NotNull Object key, String operation, Supplier<T> getter) {
        final ISpan parentSpan = Sentry.getSpan();
        if (parentSpan == null) {
            return getter.get();
        } else {
            ISpan childSpan = parentSpan.startChild(operation, getName() + " " + key);

            childSpan.setData("cache.name", getName());
            childSpan.setData("cache.key", key instanceof Collection<?> ? key : List.of(key));
            try {
                var value = getter.get();
                if (operation.equals("cache.get")) {
                    childSpan.setData("cache.hit", value != null);
                }

                // Set size of the cached value
                childSpan.setData("cache.item_size", 123);

                return value;
            } finally {
                childSpan.finish();
            }
        }
    }

    @Override
    public ValueWrapper get(@NotNull Object key) {
        return this.trace(key, "cache.get", () -> this.delegate.get(key));
    }

    @Override
    public <T> T get(@NotNull Object key, Class<T> type) {
        return this.trace(key, "cache.get", () -> this.delegate.get(key, type));
    }

    @Override
    public <T> T get(@NotNull Object key, @NotNull Callable<T> valueLoader) {
        return this.trace(key, "cache.get", () -> this.delegate.get(key, valueLoader));
    }

    @Override
    public void put(@NotNull Object key, Object value) {
        this.trace(key, "cache.put", () -> {
            this.delegate.put(key, value);
            return null;
        });
    }

    @Override
    public void evict(@NotNull Object key) {
        this.trace(key, "cache.evict", () -> {
            this.delegate.evict(key);
            return null;
        });
    }

    @Override
    public void clear() {
        this.delegate.clear();
    }
}
