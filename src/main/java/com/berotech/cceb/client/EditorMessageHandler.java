package com.berotech.cceb.client;

import org.java_websocket.WebSocket;

import com.berotech.cceb.CCEditorBridge;
import com.berotech.cceb.protocol.EditorMessage;
import com.berotech.cceb.protocol.EditorMessageCodec;
import com.berotech.cceb.protocol.MessageType;

public final class EditorMessageHandler {
    static final int MAX_MESSAGE_LENGTH = 65_536;

    private EditorMessageHandler() {}

    public static void sendHello(WebSocket connection) {
        send(connection, EditorMessage.hello("CC Editor Bridge " + CCEditorBridge.MODID));
    }

    public static void handle(WebSocket connection, String rawMessage) {
        if (rawMessage.length() > MAX_MESSAGE_LENGTH) {
            sendError(connection, "Message too large");
            return;
        }

        EditorMessage message;
        try {
            message = EditorMessageCodec.decode(rawMessage);
        } catch (IllegalArgumentException exception) {
            sendError(connection, exception.getMessage());
            return;
        }

        switch (message.type()) {
            case PING -> send(connection, EditorMessage.pong());
            case HELLO -> send(connection, EditorMessage.hello("CC Editor Bridge " + CCEditorBridge.MODID));
            case PONG, ERROR -> sendError(connection, "Unexpected message type: " + message.type().wireName());
        }
    }

    private static void send(WebSocket connection, EditorMessage message) {
        connection.send(EditorMessageCodec.encode(message));
    }

    private static void sendError(WebSocket connection, String errorMessage) {
        CCEditorBridge.LOGGER.debug("Editor protocol error: {}", errorMessage);
        send(connection, EditorMessage.error(errorMessage));
    }
}
