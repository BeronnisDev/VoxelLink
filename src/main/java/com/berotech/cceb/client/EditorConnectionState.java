package com.berotech.cceb.client;

public final class EditorConnectionState {
    private boolean authenticated;

    public EditorConnectionState(boolean authenticated) {
        this.authenticated = authenticated;
    }

    public boolean isAuthenticated() {
        return authenticated;
    }

    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
    }
}
