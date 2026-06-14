package com.berotech.voxellink.network.client;

import com.berotech.voxellink.VoxelLink;
import com.berotech.voxellink.network.payload.TargetListResponsePayload;
import com.berotech.voxellink.network.payload.ErrorResponsePayload;
import com.berotech.voxellink.network.payload.FileDeleteResponsePayload;
import com.berotech.voxellink.network.payload.FileEventPayload;
import com.berotech.voxellink.network.payload.FileListResponsePayload;
import com.berotech.voxellink.network.payload.FileReadResponsePayload;
import com.berotech.voxellink.network.payload.FileWriteResponsePayload;
import com.berotech.voxellink.network.payload.OpenEditorPayload;
import com.berotech.voxellink.client.EditorBridgeService;

import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class ClientResponseHandler {
    private ClientResponseHandler() {}

    public static void handleFileList(FileListResponsePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            VoxelLink.LOGGER.info("Received file list response for request {}", payload.requestId());
            PendingRequests.complete(payload.requestId(), payload);
        });
    }

    public static void handleFileRead(FileReadResponsePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            VoxelLink.LOGGER.info("Received file read response for request {}", payload.requestId());
            PendingRequests.complete(payload.requestId(), payload);
        });
    }

    public static void handleFileWrite(FileWriteResponsePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            VoxelLink.LOGGER.info("Received file write response for request {}", payload.requestId());
            PendingRequests.complete(payload.requestId(), payload);
        });
    }

    public static void handleFileDelete(FileDeleteResponsePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            VoxelLink.LOGGER.info("Received file delete response for request {}", payload.requestId());
            PendingRequests.complete(payload.requestId(), payload);
        });
    }

    public static void handleTargetList(TargetListResponsePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            VoxelLink.LOGGER.info("Received target list response for request {}", payload.requestId());
            PendingRequests.complete(payload.requestId(), payload);
        });
    }

    public static void handleError(ErrorResponsePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> PendingRequests.fail(payload.requestId(), new BridgeResponseException(payload.message())));
    }

    public static void handleFileEvent(FileEventPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> EditorBridgeService.forwardFileEvent(payload));
    }

    public static void handleOpenEditor(OpenEditorPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> EditorBridgeService.forwardOpenRequest(payload.targetId(), payload.path()));
    }
}
