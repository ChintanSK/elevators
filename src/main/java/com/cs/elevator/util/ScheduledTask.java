package com.cs.elevator.util;

import java.util.concurrent.*;

public class ScheduledTask<T> {
    private static final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(5);
    private final Callable<T> callable;
    private Future<T> future;

    private ScheduledTask(Callable<T> callable) {
        this.callable = callable;
    }

    public static <T> ScheduledTask<T> execute(Callable<T> callable) {
        return new ScheduledTask<>(callable);
    }

    public ScheduledTask<T> withDelayOf(long delay, TimeUnit timeunit) {
        future = executorService.schedule(callable, delay, timeunit);
        return this;
    }

    public ScheduledTask<T> now() {
        future = executorService.submit(callable);
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
}
