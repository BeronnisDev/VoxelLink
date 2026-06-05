package com.berotech.cceb.client;

import com.berotech.cceb.Config;

public final class EditorConnectionState {
    private boolean authenticated;
    private final EditorRateLimiter rateLimiter;

    public EditorConnectionState(boolean authenticated) {
        this.authenticated = authenticated;
        this.rateLimiter = new EditorRateLimiter(Config.MAX_OPERATIONS_PER_MINUTE.get());
    }

    public boolean isAuthenticated() {
        return authenticated;
    }

    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
    }

    public EditorRateLimiter rateLimiter() {
        return rateLimiter;
    }
}
