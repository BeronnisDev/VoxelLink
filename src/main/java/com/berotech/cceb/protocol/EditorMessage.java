package com.berotech.cceb.protocol;

import java.util.List;

public record EditorMessage(
        MessageType type,
        String message,
        String token,
        String computerId,
        String path,
        String content,
        List<String> files,
        List<ComputerSummary> computers
) {
    public static EditorMessage ping() {
        return new EditorMessage(MessageType.PING, null, null, null, null, null, null, null);
    }

    public static EditorMessage pong() {
        return new EditorMessage(MessageType.PONG, null, null, null, null, null, null, null);
    }

    public static EditorMessage hello(String message) {
        return new EditorMessage(MessageType.HELLO, message, null, null, null, null, null, null);
    }

    public static EditorMessage error(String message) {
        return new EditorMessage(MessageType.ERROR, message, null, null, null, null, null, null);
    }

    public static EditorMessage auth(String token) {
        return new EditorMessage(MessageType.AUTH, null, token, null, null, null, null, null);
    }

    public static EditorMessage authOk() {
        return new EditorMessage(MessageType.AUTH_OK, null, null, null, null, null, null, null);
    }

    public static EditorMessage fileEvent(MessageType type, String computerId, String path) {
        return new EditorMessage(type, null, null, computerId, path, null, null, null);
    }

    public static EditorMessage fileListOk(String computerId, String path, List<String> files) {
        return new EditorMessage(MessageType.FILE_LIST_OK, null, null, computerId, path, null, files, null);
    }

    public static EditorMessage fileReadOk(String computerId, String path, String content) {
        return new EditorMessage(MessageType.FILE_READ_OK, null, null, computerId, path, content, null, null);
    }

    public static EditorMessage fileWriteOk(String computerId, String path) {
        return new EditorMessage(MessageType.FILE_WRITE_OK, null, null, computerId, path, null, null, null);
    }

    public static EditorMessage fileDeleteOk(String computerId, String path) {
        return new EditorMessage(MessageType.FILE_DELETE_OK, null, null, computerId, path, null, null, null);
    }

    public static EditorMessage computerListOk(List<ComputerSummary> computers) {
        return new EditorMessage(MessageType.COMPUTER_LIST_OK, null, null, null, null, null, null, computers);
    }

    public static EditorMessage openFile(String computerId, String path) {
        return new EditorMessage(MessageType.OPEN_FILE, null, null, computerId, path, null, null, null);
    }
}
