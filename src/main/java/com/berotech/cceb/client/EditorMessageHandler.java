package com.berotech.cceb.client;

import org.java_websocket.WebSocket;

import com.berotech.cceb.CCEditorBridge;
import com.berotech.cceb.protocol.EditorMessage;
import com.berotech.cceb.protocol.EditorMessageCodec;
import com.berotech.cceb.protocol.MessageType;

public final class EditorMessageHandler {
    static final int MAX_MESSAGE_LENGTH = 65_536;
    private static final int AUTH_FAILURE_CLOSE_CODE = 1008;

    private EditorMessageHandler() {}

    public static void onConnect(WebSocket connection) {
        boolean authenticated = !EditorAuth.isRequired();
        connection.setAttachment(new EditorConnectionState(authenticated));

        if (authenticated) {
            sendHello(connection);
        } else {
            send(connection, EditorMessage.hello("Authentication required"));
        }
    }

    public static void handle(WebSocket connection, String rawMessage) {
        if (rawMessage.length() > MAX_MESSAGE_LENGTH) {
            reject(connection, "Message too large");
            return;
        }

        EditorMessage message;
        try {
            message = EditorMessageCodec.decode(rawMessage);
        } catch (IllegalArgumentException exception) {
            sendError(connection, exception.getMessage());
            return;
        }

        try {
            handleDecoded(connection, message);
        } catch (RuntimeException exception) {
            CCEditorBridge.LOGGER.error("Unexpected editor protocol error", exception);
            sendError(connection, "Internal bridge error");
        }
    }

    private static void handleDecoded(WebSocket connection, EditorMessage message) {
        EditorConnectionState state = getState(connection);
        if (!state.isAuthenticated()) {
            if (message.type() != MessageType.AUTH) {
                reject(connection, "Authentication required");
                return;
            }
            handleAuth(connection, state, message);
            return;
        }

        switch (message.type()) {
            case PING -> send(connection, EditorMessage.pong());
            case HELLO -> sendHello(connection);
            case AUTH -> sendError(connection, "Already authenticated");
            case FILE_LIST -> EditorFileOperations.handleFileList(connection, message);
            case FILE_READ -> EditorFileOperations.handleFileRead(connection, message);
            case FILE_WRITE -> EditorFileOperations.handleFileWrite(connection, message);
            case FILE_DELETE -> EditorFileOperations.handleFileDelete(connection, message);
            case COMPUTER_LIST -> EditorFileOperations.handleComputerList(connection, message);
            case AUTH_OK, PONG, ERROR, FILE_CREATED, FILE_MODIFIED, FILE_DELETED, OPEN_FILE,
                    FILE_LIST_OK, FILE_READ_OK, FILE_WRITE_OK, FILE_DELETE_OK, COMPUTER_LIST_OK ->
                    sendError(connection, "Unexpected message type: " + message.type().wireName());
        }
    }

    private static void handleAuth(WebSocket connection, EditorConnectionState state, EditorMessage message) {
        if (message.token() == null || message.token().isBlank()) {
            reject(connection, "Missing auth token");
            return;
        }

        if (!EditorAuth.isValidToken(message.token())) {
            reject(connection, "Invalid auth token");
            return;
        }

        state.setAuthenticated(true);
        send(connection, EditorMessage.authOk());
        CCEditorBridge.LOGGER.info("Editor authenticated from {}", connection.getRemoteSocketAddress());
    }

    private static EditorConnectionState getState(WebSocket connection) {
        EditorConnectionState state = (EditorConnectionState) connection.getAttachment();
        if (state == null) {
            state = new EditorConnectionState(!EditorAuth.isRequired());
            connection.setAttachment(state);
        }
        return state;
    }

    private static void sendHello(WebSocket connection) {
        send(connection, EditorMessage.hello("CC Editor Bridge " + CCEditorBridge.MODID));
    }

    private static void send(WebSocket connection, EditorMessage message) {
        connection.send(EditorMessageCodec.encode(message));
    }

    private static void sendError(WebSocket connection, String errorMessage) {
        CCEditorBridge.LOGGER.debug("Editor protocol error: {}", errorMessage);
        send(connection, EditorMessage.error(errorMessage));
    }

    private static void reject(WebSocket connection, String errorMessage) {
        sendError(connection, errorMessage);
        connection.close(AUTH_FAILURE_CLOSE_CODE, errorMessage);
    }
}
