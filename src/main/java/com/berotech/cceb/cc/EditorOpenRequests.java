package com.berotech.cceb.cc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import dan200.computercraft.api.ComputerCraftAPI;
import dan200.computercraft.api.filesystem.WritableMount;

import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;

import com.berotech.cceb.network.payload.OpenEditorPayload;

public final class EditorOpenRequests {
    private EditorOpenRequests() {}

    public record Result(boolean success, String message) {}

    public static Result requestOpen(ServerLevel level, BlockPos position, String computerId, String path) {
        BlockEntity blockEntity = level.getBlockEntity(position);
        if (!CCComputerSupport.isComputerBlockEntity(blockEntity)) {
            return new Result(false, "Computer is not available");
        }

        CCComputerLookup.ResolvedComputer resolved;
        try {
            resolved = resolveComputer(blockEntity, position);
        } catch (ReflectiveOperationException exception) {
            return new Result(false, "Failed to inspect computer");
        }

        try {
            if (!fileExists(level.getServer(), resolved, path)) {
                return new Result(false, "No such file: " + path);
            }
        } catch (IOException exception) {
            return new Result(false, exception.getMessage());
        }

        List<ServerPlayer> recipients = findAccessiblePlayers(level, blockEntity);
        if (recipients.isEmpty()) {
            return new Result(false, "No online player can receive editor requests for this computer");
        }

        OpenEditorPayload payload = new OpenEditorPayload(computerId, path);
        for (ServerPlayer player : recipients) {
            player.connection.send(payload);
        }

        return new Result(true, null);
    }

    public static String encodeComputerId(ServerLevel level, BlockPos position) {
        // Server-side requests always use position ids. Client config is not loaded on dedicated servers.
        return PositionComputerReference.at(level, position).encode();
    }

    private static CCComputerLookup.ResolvedComputer resolveComputer(BlockEntity blockEntity, BlockPos position)
            throws ReflectiveOperationException {
        int numericId = CCComputerSupport.getComputerId(blockEntity);
        long capacity = CCComputerSupport.getStorageCapacity(blockEntity);
        if (capacity <= 0) {
            capacity = 1_000_000L;
        }
        return new CCComputerLookup.ResolvedComputer(numericId, capacity, position);
    }

    private static boolean fileExists(MinecraftServer server, CCComputerLookup.ResolvedComputer computer, String path) throws IOException {
        WritableMount mount = ComputerCraftAPI.createSaveDirMount(server, computer.saveSubPath(), computer.storageCapacity());
        String normalizedPath = CCPaths.normalize(path);
        if (normalizedPath.isEmpty()) {
            return false;
        }
        return mount.exists(normalizedPath) && !mount.isDirectory(normalizedPath);
    }

    private static List<ServerPlayer> findAccessiblePlayers(ServerLevel level, BlockEntity blockEntity) {
        List<ServerPlayer> recipients = new ArrayList<>();
        for (ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
            if (player.serverLevel() != level) {
                continue;
            }

            try {
                if (CCComputerSupport.isUsable(blockEntity, player)) {
                    recipients.add(player);
                }
            } catch (ReflectiveOperationException ignored) {
                // Skip players we cannot evaluate.
            }
        }
        return recipients;
    }
}
