package com.berotech.cceb.client;

import java.net.InetSocketAddress;

import com.berotech.cceb.CCEditorBridge;
import com.berotech.cceb.Config;

public final class EditorBridgeService {
    private static EditorSocketServer server;
    private static volatile boolean shutdownHookRegistered;

    private EditorBridgeService() {}

    public static void startIfEnabled() {
        stop();

        if (!Config.ENABLED.get()) {
            CCEditorBridge.LOGGER.info("Editor bridge disabled in config");
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
            CCEditorBridge.LOGGER.info("Editor WebSocket server stopped");
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            CCEditorBridge.LOGGER.warn("Interrupted while stopping editor WebSocket server", exception);
        }
    }

    public static boolean isRunning() {
        return server != null;
    }

    private static void registerShutdownHook() {
        if (shutdownHookRegistered) {
            return;
        }
        Runtime.getRuntime().addShutdownHook(new Thread(EditorBridgeService::stop, "cceditor-bridge-shutdown"));
        shutdownHookRegistered = true;
    }
}
