package io.papermc.hangar.service;

import io.papermc.hangar.config.hangar.HangarConfig;
import io.papermc.hangar.util.CacheWrapper;
import jakarta.annotation.Nullable;
import jakarta.annotation.PreDestroy;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.jgroups.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Service
public class ReplicationService implements Receiver {

    private static final Logger log = LoggerFactory.getLogger(ReplicationService.class);

    private final CaffeineCacheManager cacheManager;

    @Nullable
    private final JChannel channel;
    private View lastView;

    public ReplicationService(@Lazy final CaffeineCacheManager cacheManager, final HangarConfig config) {
        this.cacheManager = cacheManager;

        if (config.isDisableJGroups()) {
            log.warn("JGroups is disabled, replication of cache clearing will not work!");
            this.channel = null;
            return;
        }

        try {
            this.channel = new JChannel(config.isDev() ? "jgroups-local.xml": "jgroups-kube.xml");
            this.channel.setReceiver(this);
            this.channel.connect("hangar-replication");
        } catch (final Exception e) {
            log.error("Error while starting JGroups");
            throw new RuntimeException(e);
        }
    }

    @PreDestroy
    public void destroy() {
        if (this.channel != null) {
            this.channel.close();
        }
    }

    @Override
    public void viewAccepted(final View newView) {
        if (this.lastView == null) {
            log.debug("Received initial view: {}", this.viewToString(newView.getMembers()));
        } else {
            log.debug("Received new view.");

            final List<Address> newMembers = View.newMembers(this.lastView, newView);
            if (!newMembers.isEmpty()) {
                log.debug("New members: {}", this.viewToString(newMembers));
            }

            final List<Address> exMembers = View.leftMembers(this.lastView, newView);
            if (!exMembers.isEmpty()) {
                log.debug("Exited members: {}", this.viewToString(exMembers));
            }
        }
        this.lastView = newView;
    }

    private String viewToString(final List<Address> view) {
        return view.stream().map(Objects::toString).collect(Collectors.joining(", "));
    }

    @Override
    public void receive(final Message msg) {
        if (this.channel == null) {
            return;
        }
        if (msg.getSrc().equals(this.channel.getAddress())) {
            return;
        }
        final String content = new String(msg.getArray(), StandardCharsets.UTF_8);
        log.debug("got message from {}: {}", msg.getSrc(), content);
        final String[] parts = content.split(":");
        switch (parts[0]) {
            case "cacheRemove" -> this.remoteRemove(parts[1], parts[2]);
            case "cacheRemoveAll" -> this.remoteRemove(parts[1], null);
            default -> log.warn("unknown message: {}", parts[0]);
        }
    }

    public void notifyRemove(final String cacheName, final Object cacheKey) {
        if (this.channel == null) {
            return;
        }

        log.debug("cache notifyRemove: {}, cacheKey: {}", cacheName, cacheKey);
        final String msg = "cacheRemove:" + cacheName + ":" + cacheKey;
        try {
            this.channel.send(new BytesMessage(null, msg.getBytes(StandardCharsets.UTF_8)));
        } catch (final Exception e) {
            log.warn("Error while sending " + msg, e);
        }
    }

    public void notifyRemoveAll(final String cacheName) {
        if (this.channel == null) {
            return;
        }

        log.debug("cache notifyRemoveAll: {}", cacheName);
        final String msg = "cacheRemoveAll:" + cacheName;
        try {
            this.channel.send(new BytesMessage(null, msg.getBytes(StandardCharsets.UTF_8)));
        } catch (final Exception e) {
            log.warn("Error while sending " + msg, e);
        }
    }

    public void remoteRemove(final String cacheName, final Object cacheKey) {
        final Cache springCache = this.cacheManager.getCache(cacheName);
        if (springCache == null) {
            log.warn("unknown cache {}", cacheName);
            return;
        }

        // if this is a wrapper, we need to unwrap, else we would loop
        //noinspection unchecked
        com.github.benmanes.caffeine.cache.Cache<Object, Object> cache = (com.github.benmanes.caffeine.cache.Cache<Object, Object>) springCache.getNativeCache();
        if (cache instanceof final CacheWrapper<Object,Object> wrapper) {
            cache = wrapper.getCache();
        }

        if (cacheKey != null) {
            log.debug("cache remoteRemove: {}, cacheKey, {}", cacheName, cacheKey);
            cache.invalidate(cacheKey);
        } else {
            log.debug("cache remoteRemoveAll: {}", cacheName);
            cache.invalidateAll();
        }
    }
}
