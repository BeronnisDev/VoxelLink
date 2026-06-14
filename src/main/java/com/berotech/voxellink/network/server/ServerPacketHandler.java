package com.berotech.voxellink.network.server;

import java.util.List;

import com.berotech.voxellink.VoxelLink;
import com.berotech.voxellink.cc.CCPaths;
import com.berotech.voxellink.network.payload.ErrorResponsePayload;
import com.berotech.voxellink.network.payload.FileDeleteRequestPayload;
import com.berotech.voxellink.network.payload.FileDeleteResponsePayload;
import com.berotech.voxellink.network.payload.FileEventType;
import com.berotech.voxellink.network.payload.FileListRequestPayload;
import com.berotech.voxellink.network.payload.FileListResponsePayload;
import com.berotech.voxellink.network.payload.FileReadRequestPayload;
import com.berotech.voxellink.network.payload.FileReadResponsePayload;
import com.berotech.voxellink.network.payload.FileWriteRequestPayload;
import com.berotech.voxellink.network.payload.FileWriteResponsePayload;
import com.berotech.voxellink.network.payload.TargetListEntry;
import com.berotech.voxellink.network.payload.TargetListRequestPayload;
import com.berotech.voxellink.network.payload.TargetListResponsePayload;
import com.berotech.voxellink.sfm.SFMPaths;
import com.berotech.voxellink.target.ResolvedTarget;
import com.berotech.voxellink.target.TargetId;
import com.berotech.voxellink.target.TargetProvider;
import com.berotech.voxellink.target.TargetProviderRegistry;
import com.berotech.voxellink.target.TargetResolveResult;

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

    public static void handleTargetList(TargetListRequestPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> handleTargetListWork(payload, context));
    }

    private static void handleTargetListWork(TargetListRequestPayload payload, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player)) {
            context.reply(new TargetListResponsePayload(payload.requestId(), List.of(), "Target list requires a player"));
            return;
        }

        VoxelLink.LOGGER.info("Target list request {}", payload.requestId());

        List<TargetListEntry> targets = TargetProviderRegistry.listAccessible(player.getServer(), player).stream()
                .map(target -> new TargetListEntry(target.id(), target.label(), target.backend(), target.kind()))
                .toList();
        context.reply(new TargetListResponsePayload(payload.requestId(), targets, ""));
    }

    private static void handleFileListWork(FileListRequestPayload payload, IPayloadContext context) {
        VoxelLink.LOGGER.info(
                "File list request {} for target '{}' path '{}'",
                payload.requestId(),
                payload.targetId(),
                payload.path()
        );

        executeFileOperation(context, payload.requestId(), payload.targetId(), payload.path(), new FileOperation<List<String>>() {
            @Override
            public List<String> execute(TargetProvider provider, ResolvedTarget target, String path) {
                return provider.listFiles(server(context), target, path);
            }

            @Override
            public void replySuccess(IPayloadContext context, int requestId, List<String> result) {
                context.reply(new FileListResponsePayload(requestId, result, ""));
            }

            @Override
            public void replyFailure(IPayloadContext context, int requestId, String message) {
                context.reply(new FileListResponsePayload(requestId, List.of(), message));
            }
        });
    }

    private static void handleFileReadWork(FileReadRequestPayload payload, IPayloadContext context) {
        VoxelLink.LOGGER.info(
                "File read request {} for target '{}' path '{}'",
                payload.requestId(),
                payload.targetId(),
                payload.path()
        );

        executeFileOperation(context, payload.requestId(), payload.targetId(), payload.path(), new FileOperation<String>() {
            @Override
            public String execute(TargetProvider provider, ResolvedTarget target, String path) {
                return provider.readFile(server(context), target, path);
            }

            @Override
            public void replySuccess(IPayloadContext context, int requestId, String result) {
                context.reply(new FileReadResponsePayload(requestId, result, ""));
            }

            @Override
            public void replyFailure(IPayloadContext context, int requestId, String message) {
                context.reply(new FileReadResponsePayload(requestId, "", message));
            }
        });
    }

    private static void handleFileWriteWork(FileWriteRequestPayload payload, IPayloadContext context) {
        VoxelLink.LOGGER.info(
                "File write request {} for target '{}' path '{}' ({} bytes)",
                payload.requestId(),
                payload.targetId(),
                payload.path(),
                payload.content().length()
        );

        executeFileOperation(context, payload.requestId(), payload.targetId(), payload.path(), new FileOperation<Void>() {
            @Override
            public Void execute(TargetProvider provider, ResolvedTarget target, String path) {
                provider.writeFile(server(context), target, path, payload.content());
                return null;
            }

            @Override
            public void replySuccess(IPayloadContext context, int requestId, Void ignored) {
                context.reply(new FileWriteResponsePayload(requestId, ""));
                FileEventEmitter.sendToPlayer(context, FileEventType.MODIFIED, payload.targetId(), payload.path());
            }

            @Override
            public void replyFailure(IPayloadContext context, int requestId, String message) {
                context.reply(new FileWriteResponsePayload(requestId, message));
            }
        });
    }

    private static void handleFileDeleteWork(FileDeleteRequestPayload payload, IPayloadContext context) {
        VoxelLink.LOGGER.info(
                "File delete request {} for target '{}' path '{}'",
                payload.requestId(),
                payload.targetId(),
                payload.path()
        );

        executeFileOperation(context, payload.requestId(), payload.targetId(), payload.path(), new FileOperation<Void>() {
            @Override
            public Void execute(TargetProvider provider, ResolvedTarget target, String path) {
                provider.deleteFile(server(context), target, path);
                return null;
            }

            @Override
            public void replySuccess(IPayloadContext context, int requestId, Void ignored) {
                context.reply(new FileDeleteResponsePayload(requestId, ""));
                FileEventEmitter.sendToPlayer(context, FileEventType.DELETED, payload.targetId(), payload.path());
            }

            @Override
            public void replyFailure(IPayloadContext context, int requestId, String message) {
                context.reply(new FileDeleteResponsePayload(requestId, message));
            }
        });
    }

    private static <T> void executeFileOperation(
            IPayloadContext context,
            int requestId,
            String targetId,
            String path,
            FileOperation<T> operation
    ) {
        if (!(context.player() instanceof ServerPlayer player)) {
            operation.replyFailure(context, requestId, "File operations require a player");
            return;
        }

        try {
            normalizePath(targetId, path);
        } catch (IllegalArgumentException exception) {
            operation.replyFailure(context, requestId, exception.getMessage());
            return;
        }

        TargetResolveResult resolve = TargetProviderRegistry.resolve(player.getServer(), player, targetId);
        if (!(resolve instanceof TargetResolveResult.Success success)) {
            String message = resolve instanceof TargetResolveResult.Failure failure
                    ? failure.message()
                    : "Cannot access target '" + targetId + "'";
            operation.replyFailure(context, requestId, message);
            return;
        }

        TargetId parsed;
        try {
            parsed = TargetId.parse(targetId);
        } catch (IllegalArgumentException exception) {
            operation.replyFailure(context, requestId, exception.getMessage());
            return;
        }

        TargetProvider provider = TargetProviderRegistry.forNamespace(parsed.namespace()).orElse(null);
        if (provider == null) {
            operation.replyFailure(context, requestId, "Backend '" + parsed.namespace() + "' is not available");
            return;
        }

        try {
            T result = operation.execute(provider, success.target(), path);
            operation.replySuccess(context, requestId, result);
        } catch (IllegalArgumentException exception) {
            operation.replyFailure(context, requestId, exception.getMessage());
        } catch (Exception exception) {
            operation.replyFailure(context, requestId, exception.getMessage());
        }
    }

    private static void normalizePath(String targetId, String path) {
        TargetId parsed = TargetId.parse(targetId);
        switch (parsed.namespace()) {
            case "cc" -> CCPaths.normalize(path == null ? "/" : path);
            case "sfm" -> SFMPaths.normalize(path == null ? "/" : path);
            default -> throw new IllegalArgumentException("Unknown backend in target id: " + targetId);
        }
    }

    public static void sendError(IPayloadContext context, int requestId, String message) {
        context.enqueueWork(() -> context.reply(new ErrorResponsePayload(requestId, message)));
    }

    private static net.minecraft.server.MinecraftServer server(IPayloadContext context) {
        return ((ServerPlayer) context.player()).getServer();
    }

    private interface FileOperation<T> {
        T execute(TargetProvider provider, ResolvedTarget target, String path);

        void replySuccess(IPayloadContext context, int requestId, T result);

        void replyFailure(IPayloadContext context, int requestId, String message);
    }
}
