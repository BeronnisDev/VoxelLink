package com.berotech.cceb.client;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class EditorRateLimiterTest {
    @Test
    void unlimitedWhenConfiguredToZero() {
        EditorRateLimiter limiter = new EditorRateLimiter(0);
        for (int i = 0; i < 200; i++) {
            assertTrue(limiter.tryAcquire());
        }
    }

    @Test
    void enforcesLimit() {
        EditorRateLimiter limiter = new EditorRateLimiter(2);
        assertTrue(limiter.tryAcquire());
        assertTrue(limiter.tryAcquire());
        assertFalse(limiter.tryAcquire());
    }
}
