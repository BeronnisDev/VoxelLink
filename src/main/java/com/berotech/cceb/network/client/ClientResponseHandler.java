package com.berotech.cceb.network.client;

import com.berotech.cceb.CCEditorBridge;
import com.berotech.cceb.network.payload.ErrorResponsePayload;
import com.berotech.cceb.network.payload.FileDeleteResponsePayload;
import com.berotech.cceb.network.payload.FileEventPayload;
import com.berotech.cceb.network.payload.FileListResponsePayload;
import com.berotech.cceb.network.payload.FileReadResponsePayload;
import com.berotech.cceb.network.payload.FileWriteResponsePayload;
import com.berotech.cceb.client.EditorBridgeService;

import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class ClientResponseHandler {
    private ClientResponseHandler() {}

    public static void handleFileList(FileListResponsePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            CCEditorBridge.LOGGER.info("Received file list response for request {}", payload.requestId());
            PendingRequests.complete(payload.requestId(), payload);
        });
    }

    public static void handleFileRead(FileReadResponsePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            CCEditorBridge.LOGGER.info("Received file read response for request {}", payload.requestId());
            PendingRequests.complete(payload.requestId(), payload);
        });
    }

    public static void handleFileWrite(FileWriteResponsePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            CCEditorBridge.LOGGER.info("Received file write response for request {}", payload.requestId());
            PendingRequests.complete(payload.requestId(), payload);
        });
    }

    public static void handleFileDelete(FileDeleteResponsePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            CCEditorBridge.LOGGER.info("Received file delete response for request {}", payload.requestId());
            PendingRequests.complete(payload.requestId(), payload);
        });
    }

    public static void handleError(ErrorResponsePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> PendingRequests.fail(payload.requestId(), new BridgeResponseException(payload.message())));
    }

    public static void handleFileEvent(FileEventPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> EditorBridgeService.forwardFileEvent(payload));
    }
}
