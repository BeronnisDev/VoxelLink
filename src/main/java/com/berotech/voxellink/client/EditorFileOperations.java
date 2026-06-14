package com.berotech.voxellink.client;

import java.util.List;

import org.java_websocket.WebSocket;

import com.berotech.voxellink.cc.CCPaths;
import com.berotech.voxellink.network.client.ClientPacketSender;
import com.berotech.voxellink.protocol.EditorMessage;
import com.berotech.voxellink.protocol.EditorMessageCodec;
import com.berotech.voxellink.sfm.SFMPaths;
import com.berotech.voxellink.target.TargetSummary;

import net.minecraft.client.Minecraft;

public final class EditorFileOperations {
    private EditorFileOperations() {}

    public static void handleFileList(WebSocket connection, EditorMessage message) {
        if (!preflight(connection)) {
            return;
        }
        if (message.targetId() == null || message.targetId().isBlank()) {
            replyError(connection, "Missing targetId");
            return;
        }

        String path = message.path() == null ? "/" : message.path();
        try {
            validatePath(message.targetId(), path);
        } catch (IllegalArgumentException exception) {
            replyError(connection, exception.getMessage());
            return;
        }

        runOnClientThread(() -> ClientPacketSender.sendFileListRequest(message.targetId(), path)
                .whenComplete((response, error) -> runOnClientThread(() -> {
                    if (error != null) {
                        replyError(connection, formatRequestError(error));
                        return;
                    }
                    if (!response.isSuccess()) {
                        replyError(connection, response.errorMessage());
                        return;
                    }
                    reply(connection, EditorMessage.fileListOk(message.targetId(), path, response.files()));
                })));
    }

    public static void handleFileRead(WebSocket connection, EditorMessage message) {
        if (!preflight(connection)) {
            return;
        }
        if (message.targetId() == null || message.targetId().isBlank()) {
            replyError(connection, "Missing targetId");
            return;
        }
        if (message.path() == null || message.path().isBlank()) {
            replyError(connection, "Missing path");
            return;
        }

        try {
            validatePath(message.targetId(), message.path());
        } catch (IllegalArgumentException exception) {
            replyError(connection, exception.getMessage());
            return;
        }

        runOnClientThread(() -> ClientPacketSender.sendFileReadRequest(message.targetId(), message.path())
                .whenComplete((response, error) -> runOnClientThread(() -> {
                    if (error != null) {
                        replyError(connection, formatRequestError(error));
                        return;
                    }
                    if (!response.isSuccess()) {
                        replyError(connection, response.errorMessage());
                        return;
                    }
                    reply(connection, EditorMessage.fileReadOk(message.targetId(), message.path(), response.content()));
                })));
    }

    public static void handleFileWrite(WebSocket connection, EditorMessage message) {
        if (!preflight(connection)) {
            return;
        }
        if (message.targetId() == null || message.targetId().isBlank()) {
            replyError(connection, "Missing targetId");
            return;
        }
        if (message.path() == null || message.path().isBlank()) {
            replyError(connection, "Missing path");
            return;
        }
        if (message.content() == null) {
            replyError(connection, "Missing content");
            return;
        }

        try {
            validatePath(message.targetId(), message.path());
        } catch (IllegalArgumentException exception) {
            replyError(connection, exception.getMessage());
            return;
        }

        runOnClientThread(() -> ClientPacketSender.sendFileWriteRequest(message.targetId(), message.path(), message.content())
                .whenComplete((response, error) -> runOnClientThread(() -> {
                    if (error != null) {
                        replyError(connection, formatRequestError(error));
                        return;
                    }
                    if (!response.isSuccess()) {
                        replyError(connection, response.errorMessage());
                        return;
                    }
                    reply(connection, EditorMessage.fileWriteOk(message.targetId(), message.path()));
                })));
    }

    public static void handleFileDelete(WebSocket connection, EditorMessage message) {
        if (!preflight(connection)) {
            return;
        }
        if (message.targetId() == null || message.targetId().isBlank()) {
            replyError(connection, "Missing targetId");
            return;
        }
        if (message.path() == null || message.path().isBlank()) {
            replyError(connection, "Missing path");
            return;
        }

        try {
            validatePath(message.targetId(), message.path());
        } catch (IllegalArgumentException exception) {
            replyError(connection, exception.getMessage());
            return;
        }

        runOnClientThread(() -> ClientPacketSender.sendFileDeleteRequest(message.targetId(), message.path())
                .whenComplete((response, error) -> runOnClientThread(() -> {
                    if (error != null) {
                        replyError(connection, formatRequestError(error));
                        return;
                    }
                    if (!response.isSuccess()) {
                        replyError(connection, response.errorMessage());
                        return;
                    }
                    reply(connection, EditorMessage.fileDeleteOk(message.targetId(), message.path()));
                })));
    }

    public static void handleTargetList(WebSocket connection, EditorMessage message) {
        if (!preflight(connection)) {
            return;
        }

        runOnClientThread(() -> ClientPacketSender.sendTargetListRequest()
                .whenComplete((response, error) -> runOnClientThread(() -> {
                    if (error != null) {
                        replyError(connection, formatRequestError(error));
                        return;
                    }
                    if (!response.isSuccess()) {
                        replyError(connection, response.errorMessage());
                        return;
                    }

                    List<TargetSummary> targets = response.targets().stream()
                            .map(entry -> TargetSummary.of(entry.id(), entry.label(), entry.backend(), entry.kind()))
                            .toList();
                    reply(connection, EditorMessage.targetListOk(targets));
                })));
    }

    private static void validatePath(String targetId, String path) {
        if (targetId.startsWith("cc:")) {
            CCPaths.normalize(path);
            return;
        }
        if (targetId.startsWith("sfm:")) {
            SFMPaths.normalize(path);
            return;
        }
        throw new IllegalArgumentException("Unknown target backend in id: " + targetId);
    }

    private static boolean preflight(WebSocket connection) {
        EditorConnectionState state = (EditorConnectionState) connection.getAttachment();
        if (state != null && !state.rateLimiter().tryAcquire()) {
            replyError(connection, "Rate limit exceeded; try again later");
            return false;
        }
        if (Minecraft.getInstance().getConnection() == null) {
            replyError(connection, "Not connected to a world");
            return false;
        }
        return true;
    }

    private static String formatRequestError(Throwable error) {
        if (error.getMessage() != null && !error.getMessage().isBlank()) {
            return error.getMessage();
        }
        return "Request failed: " + error.getClass().getSimpleName();
    }

    private static void runOnClientThread(Runnable action) {
        Minecraft.getInstance().execute(action);
    }

    private static void reply(WebSocket connection, EditorMessage message) {
        if (!connection.isOpen()) {
            return;
        }
        connection.send(EditorMessageCodec.encode(message));
    }

    private static void replyError(WebSocket connection, String errorMessage) {
        reply(connection, EditorMessage.error(errorMessage));
    }
}
