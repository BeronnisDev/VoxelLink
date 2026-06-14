package com.berotech.voxellink.client;

import java.net.InetSocketAddress;

import com.berotech.voxellink.VoxelLink;
import com.berotech.voxellink.Config;
import com.berotech.voxellink.network.payload.FileEventPayload;
import com.berotech.voxellink.network.payload.FileEventType;
import com.berotech.voxellink.protocol.EditorMessage;
import com.berotech.voxellink.protocol.EditorMessageCodec;
import com.berotech.voxellink.protocol.MessageType;

public final class EditorBridgeService {
    private static EditorSocketServer server;
    private static volatile boolean shutdownHookRegistered;

    private EditorBridgeService() {}

    public static void startIfEnabled() {
        stop();

        if (!Config.ENABLED.get()) {
            VoxelLink.LOGGER.info("Editor bridge disabled in config");
            return;
        }

        registerShutdownHook();

        int port = Config.SOCKET_PORT.get();
        server = new EditorSocketServer(new InetSocketAddress("127.0.0.1", port));
        server.start();
    }

    public static void stop() {
        if (server == null) {
            return;
        }

        EditorSocketServer stopping = server;
        server = null;

        try {
            stopping.stop(1000);
            VoxelLink.LOGGER.info("Editor WebSocket server stopped");
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            VoxelLink.LOGGER.warn("Interrupted while stopping editor WebSocket server", exception);
        }
    }

    public static boolean isRunning() {
        return server != null;
    }

    public static BridgeStatus status() {
        int port = Config.SOCKET_PORT.get();
        if (server == null) {
            return new BridgeStatus(
                    Config.ENABLED.get(),
                    false,
                    port,
                    EditorAuth.isRequired(),
                    0,
                    0,
                    Config.PREFER_LABEL_IDS.get()
            );
        }

        return new BridgeStatus(
                Config.ENABLED.get(),
                true,
                server.getPort(),
                EditorAuth.isRequired(),
                server.getConnectionCount(),
                server.getAuthenticatedConnectionCount(),
                Config.PREFER_LABEL_IDS.get()
        );
    }

    public static int getPort() {
        return server == null ? Config.SOCKET_PORT.get() : server.getPort();
    }

    public static void forwardFileEvent(FileEventPayload payload) {
        if (server == null) {
            return;
        }

        MessageType messageType = switch (payload.eventType()) {
            case CREATED -> MessageType.FILE_CREATED;
            case MODIFIED -> MessageType.FILE_MODIFIED;
            case DELETED -> MessageType.FILE_DELETED;
        };

        String json = EditorMessageCodec.encode(EditorMessage.fileEvent(messageType, payload.targetId(), payload.path()));
        server.broadcastToAuthenticated(json);
        VoxelLink.LOGGER.info(
                "Forwarded {} event for target '{}' path '{}' to editor clients",
                payload.eventType(),
                payload.targetId(),
                payload.path()
        );
    }

    public static void forwardOpenRequest(String targetId, String path) {
        if (server == null) {
            VoxelLink.LOGGER.debug(
                    "Ignoring open editor request for target '{}' path '{}'; bridge not running",
                    targetId,
                    path
            );
            return;
        }

        String json = EditorMessageCodec.encode(EditorMessage.openFile(targetId, path));
        server.broadcastToAuthenticated(json);
        VoxelLink.LOGGER.info(
                "Forwarded open_file request for target '{}' path '{}' to editor clients",
                targetId,
                path
        );
    }

    private static void registerShutdownHook() {
        if (shutdownHookRegistered) {
            return;
        }
        Runtime.getRuntime().addShutdownHook(new Thread(EditorBridgeService::stop, "voxellink-shutdown"));
        shutdownHookRegistered = true;
    }
}
