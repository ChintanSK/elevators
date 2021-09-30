package com.cs.elevator.util;

import java.util.concurrent.*;

public class AsyncTaskUtils<T> {
    private static final ScheduledExecutorService SCHEDULED_THREAD_POOL = Executors.newScheduledThreadPool(2);
    private static final ExecutorService CACHED_THREAD_POOL = Executors.newCachedThreadPool();
    private final Callable<T> callable;
    private Future<T> future;

    private AsyncTaskUtils(Callable<T> callable) {
        this.callable = callable;
    }

    public static <T> AsyncTaskUtils<T> executeAsync(Callable<T> callable) {
        return new AsyncTaskUtils<>(callable);
    }

    public static AsyncTaskUtils<Void> executeAsync(VoidCallable voidCallable) {
        return new AsyncTaskUtils<>(() -> {
            voidCallable.call();
            return null;
        });
    }

    public AsyncTaskUtils<T> withDelayOf(long delay, TimeUnit timeunit) {
        future = SCHEDULED_THREAD_POOL.schedule(callable, delay, timeunit);
        return this;
    }

    public AsyncTaskUtils<T> now() {
        future = CACHED_THREAD_POOL.submit(callable);
        return this;
    }

    public T andGetResult() {
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public void cancel() {
        future.cancel(true);
    }

    @FunctionalInterface
    public interface VoidCallable {
        void call() throws Exception;
    }
}
