package com.berotech.cceb.protocol;

public record EditorMessage(MessageType type, String message, String token, String computerId, String path) {
    public static EditorMessage ping() {
        return new EditorMessage(MessageType.PING, null, null, null, null);
    }

    public static EditorMessage pong() {
        return new EditorMessage(MessageType.PONG, null, null, null, null);
    }

    public static EditorMessage hello(String message) {
        return new EditorMessage(MessageType.HELLO, message, null, null, null);
    }

    public static EditorMessage error(String message) {
        return new EditorMessage(MessageType.ERROR, message, null, null, null);
    }

    public static EditorMessage auth(String token) {
        return new EditorMessage(MessageType.AUTH, null, token, null, null);
    }

    public static EditorMessage authOk() {
        return new EditorMessage(MessageType.AUTH_OK, null, null, null, null);
    }

    public static EditorMessage fileEvent(MessageType type, String computerId, String path) {
        return new EditorMessage(type, null, null, computerId, path);
    }
}
