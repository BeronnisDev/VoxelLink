package com.berotech.cceb.client;

import com.berotech.cceb.Config;

public final class EditorAuth {
    private EditorAuth() {}

    public static boolean isRequired() {
        if (Config.SKIP_AUTH_FOR_DEV.get()) {
            return false;
        }
        return !Config.AUTH_TOKEN.get().isEmpty();
    }

    public static boolean isValidToken(String token) {
        return Config.AUTH_TOKEN.get().equals(token);
    }
}
