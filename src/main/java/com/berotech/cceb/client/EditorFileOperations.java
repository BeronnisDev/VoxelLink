package com.berotech.cceb.client;

import org.java_websocket.WebSocket;

import com.berotech.cceb.network.client.ClientPacketSender;
import com.berotech.cceb.protocol.EditorMessage;
import com.berotech.cceb.protocol.EditorMessageCodec;

import net.minecraft.client.Minecraft;

public final class EditorFileOperations {
    private EditorFileOperations() {}

    public static void handleFileList(WebSocket connection, EditorMessage message) {
        if (message.computerId() == null || message.computerId().isBlank()) {
            replyError(connection, "Missing computerId");
            return;
        }

        String path = message.path() == null ? "/" : message.path();
        runOnClientThread(() -> ClientPacketSender.sendFileListRequest(message.computerId(), path)
                .whenComplete((response, error) -> runOnClientThread(() -> {
                    if (error != null) {
                        replyError(connection, error.getMessage());
                        return;
                    }
                    if (!response.isSuccess()) {
                        replyError(connection, response.errorMessage());
                        return;
                    }
                    reply(connection, EditorMessage.fileListOk(message.computerId(), path, response.files()));
                })));
    }

    public static void handleFileRead(WebSocket connection, EditorMessage message) {
        if (message.computerId() == null || message.computerId().isBlank()) {
            replyError(connection, "Missing computerId");
            return;
        }
        if (message.path() == null || message.path().isBlank()) {
            replyError(connection, "Missing path");
            return;
        }

        runOnClientThread(() -> ClientPacketSender.sendFileReadRequest(message.computerId(), message.path())
                .whenComplete((response, error) -> runOnClientThread(() -> {
                    if (error != null) {
                        replyError(connection, error.getMessage());
                        return;
                    }
                    if (!response.isSuccess()) {
                        replyError(connection, response.errorMessage());
                        return;
                    }
                    reply(connection, EditorMessage.fileReadOk(message.computerId(), message.path(), response.content()));
                })));
    }

    public static void handleFileWrite(WebSocket connection, EditorMessage message) {
        if (message.computerId() == null || message.computerId().isBlank()) {
            replyError(connection, "Missing computerId");
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

        runOnClientThread(() -> ClientPacketSender.sendFileWriteRequest(message.computerId(), message.path(), message.content())
                .whenComplete((response, error) -> runOnClientThread(() -> {
                    if (error != null) {
                        replyError(connection, error.getMessage());
                        return;
                    }
                    if (!response.isSuccess()) {
                        replyError(connection, response.errorMessage());
                        return;
                    }
                    reply(connection, EditorMessage.fileWriteOk(message.computerId(), message.path()));
                })));
    }

    public static void handleFileDelete(WebSocket connection, EditorMessage message) {
        if (message.computerId() == null || message.computerId().isBlank()) {
            replyError(connection, "Missing computerId");
            return;
        }
        if (message.path() == null || message.path().isBlank()) {
            replyError(connection, "Missing path");
            return;
        }

        runOnClientThread(() -> ClientPacketSender.sendFileDeleteRequest(message.computerId(), message.path())
                .whenComplete((response, error) -> runOnClientThread(() -> {
                    if (error != null) {
                        replyError(connection, error.getMessage());
                        return;
                    }
                    if (!response.isSuccess()) {
                        replyError(connection, response.errorMessage());
                        return;
                    }
                    reply(connection, EditorMessage.fileDeleteOk(message.computerId(), message.path()));
                })));
    }

    private static void runOnClientThread(Runnable action) {
        Minecraft.getInstance().execute(action);
    }

    private static void reply(WebSocket connection, EditorMessage message) {
        connection.send(EditorMessageCodec.encode(message));
    }

    private static void replyError(WebSocket connection, String errorMessage) {
        reply(connection, EditorMessage.error(errorMessage));
    }
}
