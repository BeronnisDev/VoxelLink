package com.berotech.voxellink.network.client;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import com.berotech.voxellink.VoxelLink;

public final class PendingRequests {
    private static final AtomicInteger NEXT_ID = new AtomicInteger(1);
    private static final Map<Integer, CompletableFuture<?>> PENDING = new ConcurrentHashMap<>();

    private PendingRequests() {}

    public static int nextId() {
        return NEXT_ID.getAndIncrement();
    }

    public static <T> CompletableFuture<T> register(int requestId, long timeoutSeconds) {
        CompletableFuture<T> future = new CompletableFuture<>();
        PENDING.put(requestId, future);
        future.orTimeout(timeoutSeconds, TimeUnit.SECONDS).whenComplete((result, error) -> {
            PENDING.remove(requestId, future);
            if (error instanceof TimeoutException) {
                VoxelLink.LOGGER.warn("Bridge packet request {} timed out", requestId);
            }
        });
        return future;
    }

    @SuppressWarnings("unchecked")
    public static <T> void complete(int requestId, T response) {
        CompletableFuture<?> future = PENDING.remove(requestId);
        if (future != null) {
            ((CompletableFuture<T>) future).complete(response);
        }
    }

    public static void fail(int requestId, Throwable error) {
        CompletableFuture<?> future = PENDING.remove(requestId);
        if (future != null) {
            future.completeExceptionally(error);
        }
    }
}
