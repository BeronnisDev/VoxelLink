package com.berotech.cceb.protocol;

public record EditorMessage(MessageType type, String message, String token) {
    public static EditorMessage ping() {
        return new EditorMessage(MessageType.PING, null, null);
    }

    public static EditorMessage pong() {
        return new EditorMessage(MessageType.PONG, null, null);
    }

    public static EditorMessage hello(String message) {
        return new EditorMessage(MessageType.HELLO, message, null);
    }

    public static EditorMessage error(String message) {
        return new EditorMessage(MessageType.ERROR, message, null);
    }

    public static EditorMessage auth(String token) {
        return new EditorMessage(MessageType.AUTH, null, token);
    }

    public static EditorMessage authOk() {
        return new EditorMessage(MessageType.AUTH_OK, null, null);
    }
}
