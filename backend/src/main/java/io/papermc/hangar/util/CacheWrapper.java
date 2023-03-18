package io.papermc.hangar.util;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Policy;
import com.github.benmanes.caffeine.cache.stats.CacheStats;
import io.papermc.hangar.service.ReplicationService;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.checker.nullness.qual.PolyNull;

public class CacheWrapper<K, V> implements Cache<K, V> {

    private final String name;
    private final Cache<K, V> delegate;
    private final ReplicationService replicationService;

    public CacheWrapper(final String name, final Cache<K, V> delegate, final ReplicationService replicationService) {
        this.name = name;
        this.delegate = delegate;
        this.replicationService = replicationService;
    }

    @Override
    public @Nullable V getIfPresent(final K key) {
        return this.delegate.getIfPresent(key);
    }

    @Override
    public @PolyNull V get(final K key, final Function<? super K, ? extends @PolyNull V> mappingFunction) {
        return this.delegate.get(key, mappingFunction);
    }

    @Override
    public Map<K, V> getAllPresent(final Iterable<? extends K> keys) {
        return this.delegate.getAllPresent(keys);
    }

    @Override
    public Map<K, V> getAll(final Iterable<? extends K> keys, final Function<? super Set<? extends K>, ? extends Map<? extends K, ? extends V>> mappingFunction) {
        return this.delegate.getAll(keys, mappingFunction);
    }

    @Override
    public void put(final K key, final V value) {
        this.delegate.put(key, value);
    }

    @Override
    public void putAll(final Map<? extends K, ? extends V> map) {
        this.delegate.putAll(map);
    }

    @Override
    public void invalidate(final K key) {
        this.replicationService.notifyRemove(this.name, key);
        this.delegate.invalidate(key);
    }

    @Override
    public void invalidateAll(final Iterable<? extends K> keys) {
        this.replicationService.notifyRemoveAll(this.name);
        this.delegate.invalidateAll(keys);
    }

    @Override
    public void invalidateAll() {
        this.replicationService.notifyRemoveAll(this.name);
        this.delegate.invalidateAll();
    }

    @Override
    public @NonNegative long estimatedSize() {
        return this.delegate.estimatedSize();
    }

    @Override
    public CacheStats stats() {
        return this.delegate.stats();
    }

    @Override
    public ConcurrentMap<K, V> asMap() {
        return this.delegate.asMap();
    }

    @Override
    public void cleanUp() {
        this.delegate.cleanUp();
    }

    @Override
    public Policy<K, V> policy() {
        return this.delegate.policy();
    }

    public Cache<K, V> getCache() {
        return this.delegate;
    }
}
