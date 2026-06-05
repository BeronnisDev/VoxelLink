package com.berotech.cceb.cc;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;

public final class CCComputerLookup {
    private static final long DEFAULT_CAPACITY = 1_000_000L;
    private static final String SERVER_CONTEXT = "dan200.computercraft.shared.computer.core.ServerContext";
    private static final String SERVER_COMPUTER = "dan200.computercraft.shared.computer.core.ServerComputer";

    private static final Method SERVER_CONTEXT_GET;
    private static final Method SERVER_CONTEXT_REGISTRY;
    private static final Method SERVER_COMPUTER_REGISTRY_GET_COMPUTERS;
    private static final Method SERVER_COMPUTER_GET_LABEL;
    private static final Method SERVER_COMPUTER_GET_POSITION;
    private static final Method SERVER_COMPUTER_GET_LEVEL;

    static {
        try {
            Class<?> serverContext = Class.forName(SERVER_CONTEXT);
            Class<?> serverComputer = Class.forName(SERVER_COMPUTER);
            Class<?> registryClass = Class.forName("dan200.computercraft.shared.computer.core.ServerComputerRegistry");

            SERVER_CONTEXT_GET = serverContext.getMethod("get", MinecraftServer.class);
            SERVER_CONTEXT_REGISTRY = serverContext.getMethod("registry");
            SERVER_COMPUTER_REGISTRY_GET_COMPUTERS = registryClass.getMethod("getComputers");
            SERVER_COMPUTER_GET_LABEL = serverComputer.getMethod("getLabel");
            SERVER_COMPUTER_GET_POSITION = serverComputer.getMethod("getPosition");
            SERVER_COMPUTER_GET_LEVEL = serverComputer.getMethod("getLevel");
        } catch (ReflectiveOperationException exception) {
            throw new ExceptionInInitializerError(exception);
        }
    }

    private CCComputerLookup() {}

    public record ResolvedComputer(int numericId, long storageCapacity, BlockPos position) {
        public String saveSubPath() {
            return "computer/" + numericId;
        }
    }

    public sealed interface ResolveResult {
        record Success(ResolvedComputer computer) implements ResolveResult {}

        record Failure(String message) implements ResolveResult {}
    }

    public static ResolveResult resolveDetailed(MinecraftServer server, ServerPlayer player, String computerId) {
        ComputerReference reference;
        try {
            reference = ComputerReference.parse(computerId);
        } catch (IllegalArgumentException exception) {
            return new ResolveResult.Failure(exception.getMessage());
        }

        return switch (reference) {
            case PositionComputerReference position -> resolveAtPosition(server, player, position);
            case LabelComputerReference label -> resolveByLabel(server, player, label);
        };
    }

    public static Optional<ResolvedComputer> resolve(MinecraftServer server, ServerPlayer player, String computerId) {
        return switch (resolveDetailed(server, player, computerId)) {
            case ResolveResult.Success success -> Optional.of(success.computer());
            case ResolveResult.Failure ignored -> Optional.empty();
        };
    }

    private static ResolveResult resolveAtPosition(MinecraftServer server, ServerPlayer player, PositionComputerReference identifier) {
        ServerLevel level = server.getLevel(identifier.dimensionKey());
        if (level == null) {
            return new ResolveResult.Failure("Unknown dimension: " + identifier.dimension());
        }

        BlockEntity blockEntity = level.getBlockEntity(identifier.position());
        if (!CCComputerSupport.isComputerBlockEntity(blockEntity)) {
            return new ResolveResult.Failure("No CC computer at " + identifier.encode());
        }

        return resolveFromBlockEntity(blockEntity, player, identifier.position());
    }

    private static ResolveResult resolveByLabel(MinecraftServer server, ServerPlayer player, LabelComputerReference labelReference) {
        Map<BlockPos, ResolvedComputer> matches = new LinkedHashMap<>();

        collectLabelMatchesFromRunningComputers(server, player, labelReference.label(), matches);
        collectLabelMatchesFromLoadedChunks(server, player, labelReference.label(), matches);

        if (matches.isEmpty()) {
            return new ResolveResult.Failure("No accessible computer with label '" + labelReference.label()
                    + "'. Set one in-game with os.setComputerLabel(\"...\").");
        }
        if (matches.size() > 1) {
            List<String> ids = matches.values().stream()
                    .map(computer -> PositionComputerReference.at(server.getLevel(player.level().dimension()), computer.position()).encode())
                    .toList();
            return new ResolveResult.Failure("Ambiguous label '" + labelReference.label() + "'. Matches: " + ids);
        }

        return new ResolveResult.Success(matches.values().iterator().next());
    }

    private static void collectLabelMatchesFromRunningComputers(
            MinecraftServer server,
            ServerPlayer player,
            String label,
            Map<BlockPos, ResolvedComputer> matches
    ) {
        try {
            Object context = SERVER_CONTEXT_GET.invoke(null, server);
            Object registry = SERVER_CONTEXT_REGISTRY.invoke(context);
            @SuppressWarnings("unchecked")
            Collection<Object> computers = (Collection<Object>) SERVER_COMPUTER_REGISTRY_GET_COMPUTERS.invoke(registry);

            for (Object computer : computers) {
                String computerLabel = (String) SERVER_COMPUTER_GET_LABEL.invoke(computer);
                if (!label.equals(computerLabel)) {
                    continue;
                }

                ServerLevel level = (ServerLevel) SERVER_COMPUTER_GET_LEVEL.invoke(computer);
                BlockPos position = (BlockPos) SERVER_COMPUTER_GET_POSITION.invoke(computer);
                BlockEntity blockEntity = level.getBlockEntity(position);
                if (!CCComputerSupport.isComputerBlockEntity(blockEntity)) {
                    continue;
                }

                ResolveResult result = resolveFromBlockEntity(blockEntity, player, position);
                if (result instanceof ResolveResult.Success success) {
                    matches.putIfAbsent(position, success.computer());
                }
            }
        } catch (ReflectiveOperationException exception) {
            // Fall back to loaded-chunk scan only.
        }
    }

    private static void collectLabelMatchesFromLoadedChunks(
            MinecraftServer server,
            ServerPlayer player,
            String label,
            Map<BlockPos, ResolvedComputer> matches
    ) {
        int viewDistance = server.getPlayerList().getViewDistance();
        for (ServerPlayer onlinePlayer : server.getPlayerList().getPlayers()) {
            ServerLevel level = onlinePlayer.serverLevel();
            ChunkPos center = new ChunkPos(onlinePlayer.blockPosition());
            for (int chunkX = center.x - viewDistance; chunkX <= center.x + viewDistance; chunkX++) {
                for (int chunkZ = center.z - viewDistance; chunkZ <= center.z + viewDistance; chunkZ++) {
                    LevelChunk chunk = level.getChunkSource().getChunkNow(chunkX, chunkZ);
                    if (chunk == null) {
                        continue;
                    }

                    scanChunkForLabel(player, label, matches, chunk);
                }
            }
        }
    }

    private static void scanChunkForLabel(
            ServerPlayer player,
            String label,
            Map<BlockPos, ResolvedComputer> matches,
            LevelChunk chunk
    ) {
        for (BlockEntity blockEntity : chunk.getBlockEntities().values()) {
            if (!CCComputerSupport.isComputerBlockEntity(blockEntity)) {
                continue;
            }

            try {
                if (!label.equals(CCComputerSupport.getLabel(blockEntity))) {
                    continue;
                }
            } catch (ReflectiveOperationException exception) {
                continue;
            }

            BlockPos position = blockEntity.getBlockPos();
            ResolveResult result = resolveFromBlockEntity(blockEntity, player, position);
            if (result instanceof ResolveResult.Success success) {
                matches.putIfAbsent(position, success.computer());
            }
        }
    }

    private static ResolveResult resolveFromBlockEntity(BlockEntity blockEntity, ServerPlayer player, BlockPos position) {
        try {
            if (!CCComputerSupport.isUsable(blockEntity, player)) {
                return new ResolveResult.Failure("You cannot access the computer at "
                        + PositionComputerReference.at(player.serverLevel(), position).encode());
            }

            int numericId = CCComputerSupport.getComputerId(blockEntity);
            if (numericId < 0) {
                return new ResolveResult.Failure("Computer at "
                        + PositionComputerReference.at(player.serverLevel(), position).encode()
                        + " has not been assigned an id yet");
            }

            long capacity = CCComputerSupport.getStorageCapacity(blockEntity);
            if (capacity <= 0) {
                capacity = DEFAULT_CAPACITY;
            }

            return new ResolveResult.Success(new ResolvedComputer(numericId, capacity, position));
        } catch (ReflectiveOperationException exception) {
            return new ResolveResult.Failure("Failed to inspect CC computer");
        }
    }
}
