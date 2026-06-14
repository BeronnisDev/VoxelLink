package com.berotech.voxellink.client;

import java.util.ArrayDeque;

public final class EditorRateLimiter {
    private static final long WINDOW_MS = 60_000L;

    private final int maxOperationsPerMinute;
    private final ArrayDeque<Long> timestamps = new ArrayDeque<>();

    public EditorRateLimiter(int maxOperationsPerMinute) {
        this.maxOperationsPerMinute = maxOperationsPerMinute;
    }

    public synchronized boolean tryAcquire() {
        if (maxOperationsPerMinute <= 0) {
            return true;
        }

        long now = System.currentTimeMillis();
        while (!timestamps.isEmpty() && now - timestamps.peekFirst() >= WINDOW_MS) {
            timestamps.removeFirst();
        }

        if (timestamps.size() >= maxOperationsPerMinute) {
            return false;
        }

        timestamps.addLast(now);
        return true;
    }
}
