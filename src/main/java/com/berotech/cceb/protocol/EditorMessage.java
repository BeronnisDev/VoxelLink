package com.berotech.cceb.protocol;

public record EditorMessage(MessageType type, String message) {
    public static EditorMessage ping() {
        return new EditorMessage(MessageType.PING, null);
    }

    public static EditorMessage pong() {
        return new EditorMessage(MessageType.PONG, null);
    }

    public static EditorMessage hello(String message) {
        return new EditorMessage(MessageType.HELLO, message);
    }

    public static EditorMessage error(String message) {
        return new EditorMessage(MessageType.ERROR, message);
    }
}
