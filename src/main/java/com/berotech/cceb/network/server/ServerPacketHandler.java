package com.berotech.cceb.network.server;

import java.util.List;

import com.berotech.cceb.CCEditorBridge;
import com.berotech.cceb.network.payload.ErrorResponsePayload;
import com.berotech.cceb.network.payload.FileDeleteRequestPayload;
import com.berotech.cceb.network.payload.FileDeleteResponsePayload;
import com.berotech.cceb.network.payload.FileEventType;
import com.berotech.cceb.network.payload.FileListRequestPayload;
import com.berotech.cceb.network.payload.FileListResponsePayload;
import com.berotech.cceb.network.payload.FileReadRequestPayload;
import com.berotech.cceb.network.payload.FileReadResponsePayload;
import com.berotech.cceb.network.payload.FileWriteRequestPayload;
import com.berotech.cceb.network.payload.FileWriteResponsePayload;

import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class ServerPacketHandler {
    private ServerPacketHandler() {}

    public static void handleFileList(FileListRequestPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            CCEditorBridge.LOGGER.info(
                    "File list request {} for computer '{}' path '{}'",
                    payload.requestId(),
                    payload.computerId(),
                    payload.path()
            );

            // Stub until CC: Tweaked integration (Phase 4)
            context.reply(new FileListResponsePayload(payload.requestId(), List.of("startup.lua", "rom/help.txt"), ""));
        });
    }

    public static void handleFileRead(FileReadRequestPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            CCEditorBridge.LOGGER.info(
                    "File read request {} for computer '{}' path '{}'",
                    payload.requestId(),
                    payload.computerId(),
                    payload.path()
            );
            context.reply(new FileReadResponsePayload(payload.requestId(), "-- stub content\nprint('hello')\n", ""));
        });
    }

    public static void handleFileWrite(FileWriteRequestPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            CCEditorBridge.LOGGER.info(
                    "File write request {} for computer '{}' path '{}' ({} bytes)",
                    payload.requestId(),
                    payload.computerId(),
                    payload.path(),
                    payload.content().length()
            );
            context.reply(new FileWriteResponsePayload(payload.requestId(), ""));
            FileEventEmitter.sendToPlayer(context, FileEventType.MODIFIED, payload.computerId(), payload.path());
        });
    }

    public static void handleFileDelete(FileDeleteRequestPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            CCEditorBridge.LOGGER.info(
                    "File delete request {} for computer '{}' path '{}'",
                    payload.requestId(),
                    payload.computerId(),
                    payload.path()
            );
            context.reply(new FileDeleteResponsePayload(payload.requestId(), ""));
            FileEventEmitter.sendToPlayer(context, FileEventType.DELETED, payload.computerId(), payload.path());
        });
    }

    public static void sendError(IPayloadContext context, int requestId, String message) {
        context.enqueueWork(() -> context.reply(new ErrorResponsePayload(requestId, message)));
    }
}
