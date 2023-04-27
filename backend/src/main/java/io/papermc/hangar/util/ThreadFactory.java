package io.papermc.hangar.util;

import io.sentry.Sentry;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.jetbrains.annotations.NotNull;

public class ThreadFactory implements java.util.concurrent.ThreadFactory {
    private static final AtomicLong POOL_COUNTER = new AtomicLong(0L);
    private final java.util.concurrent.ThreadFactory delegate = Executors.defaultThreadFactory();
    private final AtomicInteger threadCounter = new AtomicInteger(0);
    private final boolean daemonThreads;
    private final String name;

    public ThreadFactory(final String name, final boolean daemonThreads) {
        this.name = name + "-" + POOL_COUNTER.getAndIncrement() % 10_000;
        this.daemonThreads = daemonThreads;
    }

    @Override
    public Thread newThread(final @NotNull Runnable runnable) {
        final Thread thread = this.delegate.newThread(runnable);
        thread.setName(this.name + "-" + this.threadCounter.getAndIncrement());
        thread.setUncaughtExceptionHandler((t, e) -> {
            e.printStackTrace();
            Sentry.captureException(e);
        });
        if (this.daemonThreads) {
            thread.setDaemon(true);
        }
        return thread;
    }
}
