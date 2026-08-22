package io.papermc.hangar.components.observability;

import io.sentry.ISpan;
import io.sentry.Sentry;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Supplier;
import org.springframework.cache.Cache;

public class SentryCacheWrapper implements Cache {

    private final Cache delegate;

    public SentryCacheWrapper(Cache delegate) {
        this.delegate = delegate;
    }

    @Override
    public String getName() {
        return this.delegate.getName();
    }

    @Override
    public Object getNativeCache() {
        return this.delegate.getNativeCache();
    }

    private <T> T trace(Object key, String operation, Supplier<T> getter) {
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
    public ValueWrapper get(Object key) {
        return this.trace(key, "cache.get", () -> this.delegate.get(key));
    }

    @Override
    public <T> T get(Object key, Class<T> type) {
        return this.trace(key, "cache.get", () -> this.delegate.get(key, type));
    }

    @Override
    public <T> T get(Object key, Callable<T> valueLoader) {
        return this.trace(key, "cache.get", () -> this.delegate.get(key, valueLoader));
    }

    @Override
    public void put(Object key, Object value) {
        this.trace(key, "cache.put", () -> {
            this.delegate.put(key, value);
            return null;
        });
    }

    @Override
    public void evict(Object key) {
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
