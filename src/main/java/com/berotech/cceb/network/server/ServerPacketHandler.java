package com.berotech.cceb.network.server;

import java.util.List;

import com.berotech.cceb.CCEditorBridge;
import com.berotech.cceb.cc.CCComputerLookup;
import com.berotech.cceb.cc.CCFilesystemAccess;
import com.berotech.cceb.cc.CCPaths;
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

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class ServerPacketHandler {
    private ServerPacketHandler() {}

    public static void handleFileList(FileListRequestPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> handleFileListWork(payload, context));
    }

    public static void handleFileRead(FileReadRequestPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> handleFileReadWork(payload, context));
    }

    public static void handleFileWrite(FileWriteRequestPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> handleFileWriteWork(payload, context));
    }

    public static void handleFileDelete(FileDeleteRequestPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> handleFileDeleteWork(payload, context));
    }

    private static void handleFileListWork(FileListRequestPayload payload, IPayloadContext context) {
        CCEditorBridge.LOGGER.info(
                "File list request {} for computer '{}' path '{}'",
                payload.requestId(),
                payload.computerId(),
                payload.path()
        );

        resolveComputer(payload.computerId(), context).ifPresentOrElse(
                computer -> {
                    try {
                        List<String> files = CCFilesystemAccess.listFiles(server(context), computer, payload.path());
                        context.reply(new FileListResponsePayload(payload.requestId(), files, ""));
                    } catch (IllegalArgumentException exception) {
                        context.reply(new FileListResponsePayload(payload.requestId(), List.of(), exception.getMessage()));
                    } catch (Exception exception) {
                        context.reply(new FileListResponsePayload(payload.requestId(), List.of(), exception.getMessage()));
                    }
                },
                () -> context.reply(new FileListResponsePayload(payload.requestId(), List.of(), resolveError(payload.computerId(), context)))
        );
    }

    private static void handleFileReadWork(FileReadRequestPayload payload, IPayloadContext context) {
        CCEditorBridge.LOGGER.info(
                "File read request {} for computer '{}' path '{}'",
                payload.requestId(),
                payload.computerId(),
                payload.path()
        );

        resolveComputer(payload.computerId(), context).ifPresentOrElse(
                computer -> {
                    try {
                        CCPaths.normalize(payload.path());
                        String content = CCFilesystemAccess.readFile(server(context), computer, payload.path());
                        context.reply(new FileReadResponsePayload(payload.requestId(), content, ""));
                    } catch (IllegalArgumentException exception) {
                        context.reply(new FileReadResponsePayload(payload.requestId(), "", exception.getMessage()));
                    } catch (Exception exception) {
                        context.reply(new FileReadResponsePayload(payload.requestId(), "", exception.getMessage()));
                    }
                },
                () -> context.reply(new FileReadResponsePayload(payload.requestId(), "", resolveError(payload.computerId(), context)))
        );
    }

    private static void handleFileWriteWork(FileWriteRequestPayload payload, IPayloadContext context) {
        CCEditorBridge.LOGGER.info(
                "File write request {} for computer '{}' path '{}' ({} bytes)",
                payload.requestId(),
                payload.computerId(),
                payload.path(),
                payload.content().length()
        );

        resolveComputer(payload.computerId(), context).ifPresentOrElse(
                computer -> {
                    try {
                        CCPaths.normalize(payload.path());
                        CCFilesystemAccess.writeFile(server(context), computer, payload.path(), payload.content());
                        context.reply(new FileWriteResponsePayload(payload.requestId(), ""));
                        FileEventEmitter.sendToPlayer(context, FileEventType.MODIFIED, payload.computerId(), payload.path());
                    } catch (IllegalArgumentException exception) {
                        context.reply(new FileWriteResponsePayload(payload.requestId(), exception.getMessage()));
                    } catch (Exception exception) {
                        context.reply(new FileWriteResponsePayload(payload.requestId(), exception.getMessage()));
                    }
                },
                () -> context.reply(new FileWriteResponsePayload(payload.requestId(), resolveError(payload.computerId(), context)))
        );
    }

    private static void handleFileDeleteWork(FileDeleteRequestPayload payload, IPayloadContext context) {
        CCEditorBridge.LOGGER.info(
                "File delete request {} for computer '{}' path '{}'",
                payload.requestId(),
                payload.computerId(),
                payload.path()
        );

        resolveComputer(payload.computerId(), context).ifPresentOrElse(
                computer -> {
                    try {
                        CCPaths.normalize(payload.path());
                        CCFilesystemAccess.deleteFile(server(context), computer, payload.path());
                        context.reply(new FileDeleteResponsePayload(payload.requestId(), ""));
                        FileEventEmitter.sendToPlayer(context, FileEventType.DELETED, payload.computerId(), payload.path());
                    } catch (IllegalArgumentException exception) {
                        context.reply(new FileDeleteResponsePayload(payload.requestId(), exception.getMessage()));
                    } catch (Exception exception) {
                        context.reply(new FileDeleteResponsePayload(payload.requestId(), exception.getMessage()));
                    }
                },
                () -> context.reply(new FileDeleteResponsePayload(payload.requestId(), resolveError(payload.computerId(), context)))
        );
    }

    public static void sendError(IPayloadContext context, int requestId, String message) {
        context.enqueueWork(() -> context.reply(new ErrorResponsePayload(requestId, message)));
    }

    private static net.minecraft.server.MinecraftServer server(IPayloadContext context) {
        return ((ServerPlayer) context.player()).getServer();
    }

    private static java.util.Optional<CCComputerLookup.ResolvedComputer> resolveComputer(String computerId, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player)) {
            return java.util.Optional.empty();
        }
        return CCComputerLookup.resolve(player.getServer(), player, computerId);
    }

    private static String resolveError(String computerId, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player)) {
            return computerAccessError(computerId);
        }

        return switch (CCComputerLookup.resolveDetailed(player.getServer(), player, computerId)) {
            case CCComputerLookup.ResolveResult.Success ignored -> computerAccessError(computerId);
            case CCComputerLookup.ResolveResult.Failure failure -> failure.message();
        };
    }

    private static String computerAccessError(String computerId) {
        return "Cannot access computer '" + computerId + "'. Use pos:<dimension>:x:y:z or label:<name>.";
    }
}
