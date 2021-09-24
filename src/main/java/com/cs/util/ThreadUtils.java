package com.cs.util;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public final class ThreadUtils {
    public static <T> CompletableFuture<T> setTimeout(Supplier<T> task, long delay) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(delay);
                return task.get();
            } catch (InterruptedException e) {
                return null;
            }
        });
    }
}
