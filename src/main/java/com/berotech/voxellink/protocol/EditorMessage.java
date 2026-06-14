package com.berotech.voxellink.protocol;

import java.util.List;

import com.berotech.voxellink.target.TargetSummary;

public record EditorMessage(
        MessageType type,
        String message,
        String token,
        String targetId,
        String path,
        String content,
        List<String> files,
        List<TargetSummary> targets,
        List<String> backends
) {
    public static EditorMessage ping() {
        return new EditorMessage(MessageType.PING, null, null, null, null, null, null, null, null);
    }

    public static EditorMessage pong() {
        return new EditorMessage(MessageType.PONG, null, null, null, null, null, null, null, null);
    }

    public static EditorMessage hello(String message, List<String> backends) {
        return new EditorMessage(MessageType.HELLO, message, null, null, null, null, null, null, backends);
    }

    public static EditorMessage error(String message) {
        return new EditorMessage(MessageType.ERROR, message, null, null, null, null, null, null, null);
    }

    public static EditorMessage auth(String token) {
        return new EditorMessage(MessageType.AUTH, null, token, null, null, null, null, null, null);
    }

    public static EditorMessage authOk() {
        return new EditorMessage(MessageType.AUTH_OK, null, null, null, null, null, null, null, null);
    }

    public static EditorMessage fileEvent(MessageType type, String targetId, String path) {
        return new EditorMessage(type, null, null, targetId, path, null, null, null, null);
    }

    public static EditorMessage fileListOk(String targetId, String path, List<String> files) {
        return new EditorMessage(MessageType.FILE_LIST_OK, null, null, targetId, path, null, files, null, null);
    }

    public static EditorMessage fileReadOk(String targetId, String path, String content) {
        return new EditorMessage(MessageType.FILE_READ_OK, null, null, targetId, path, content, null, null, null);
    }

    public static EditorMessage fileWriteOk(String targetId, String path) {
        return new EditorMessage(MessageType.FILE_WRITE_OK, null, null, targetId, path, null, null, null, null);
    }

    public static EditorMessage fileDeleteOk(String targetId, String path) {
        return new EditorMessage(MessageType.FILE_DELETE_OK, null, null, targetId, path, null, null, null, null);
    }

    public static EditorMessage targetListOk(List<TargetSummary> targets) {
        return new EditorMessage(MessageType.TARGET_LIST_OK, null, null, null, null, null, null, targets, null);
    }

    public static EditorMessage openFile(String targetId, String path) {
        return new EditorMessage(MessageType.OPEN_FILE, null, null, targetId, path, null, null, null, null);
    }
}
