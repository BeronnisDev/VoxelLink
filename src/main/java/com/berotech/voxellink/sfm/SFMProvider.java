package com.berotech.voxellink.sfm;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.berotech.voxellink.cc.PositionComputerReference;
import com.berotech.voxellink.target.ResolvedTarget;
import com.berotech.voxellink.target.SFMResolvedTarget;
import com.berotech.voxellink.target.TargetProvider;
import com.berotech.voxellink.target.TargetResolveResult;
import com.berotech.voxellink.target.TargetSummary;

import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.fml.ModList;

public final class SFMProvider implements TargetProvider {
    public static final String NAMESPACE = "sfm";
    private static final int MAX_PROGRAM_LENGTH = 32_300;

    @Override
    public String namespace() {
        return NAMESPACE;
    }

    @Override
    public boolean isAvailable() {
        return ModList.get().isLoaded("sfm");
    }

    @Override
    public List<TargetSummary> listAccessible(MinecraftServer server, ServerPlayer player) {
        Map<BlockPos, TargetSummary> matches = new LinkedHashMap<>();
        ServerLevel level = player.serverLevel();
        int viewDistance = server.getPlayerList().getViewDistance();
        ChunkPos center = new ChunkPos(player.blockPosition());

        for (int chunkX = center.x - viewDistance; chunkX <= center.x + viewDistance; chunkX++) {
            for (int chunkZ = center.z - viewDistance; chunkZ <= center.z + viewDistance; chunkZ++) {
                LevelChunk chunk = level.getChunkSource().getChunkNow(chunkX, chunkZ);
                if (chunk == null) {
                    continue;
                }

                for (BlockEntity blockEntity : chunk.getBlockEntities().values()) {
                    tryAddManager(matches, blockEntity, player, level);
                }
            }
        }

        return List.copyOf(matches.values());
    }

    @Override
    public TargetResolveResult resolve(MinecraftServer server, ServerPlayer player, String localId) {
        PositionComputerReference reference;
        try {
            reference = PositionComputerReference.parse(localId);
        } catch (IllegalArgumentException exception) {
            return new TargetResolveResult.Failure(exception.getMessage());
        }

        ServerLevel level = server.getLevel(reference.dimensionKey());
        if (level == null) {
            return new TargetResolveResult.Failure("Unknown dimension: " + reference.dimension());
        }

        BlockEntity blockEntity = level.getBlockEntity(reference.position());
        if (!SFMSupport.isManagerBlockEntity(blockEntity)) {
            return new TargetResolveResult.Failure("No SFM manager at " + encodeTargetId(localId));
        }

        if (!canAccess(player, level, reference.position())) {
            return new TargetResolveResult.Failure("You cannot access the manager at " + encodeTargetId(localId));
        }

        if (!hasDisk(blockEntity)) {
            return new TargetResolveResult.Failure("Manager at " + encodeTargetId(localId) + " has no disk inserted");
        }

        return new TargetResolveResult.Success(new SFMResolvedTarget(level, reference.position()));
    }

    @Override
    public List<String> listFiles(MinecraftServer server, ResolvedTarget target, String path) {
        requireManager(target);
        SFMPaths.normalize(path);
        return List.of(SFMPaths.PROGRAM_FILE);
    }

    @Override
    public String readFile(MinecraftServer server, ResolvedTarget target, String path) {
        SFMResolvedTarget manager = requireManager(target);
        SFMPaths.normalize(path);
        BlockEntity blockEntity = manager.level().getBlockEntity(manager.position());
        try {
            String program = (String) SFMSupport.MANAGER_GET_PROGRAM_STRING.invoke(blockEntity);
            return program == null ? "" : program;
        } catch (ReflectiveOperationException exception) {
            throw new IllegalArgumentException("Failed to read SFM program", exception);
        }
    }

    @Override
    public void writeFile(MinecraftServer server, ResolvedTarget target, String path, String content) {
        SFMResolvedTarget manager = requireManager(target);
        SFMPaths.normalize(path);
        if (content.length() > MAX_PROGRAM_LENGTH) {
            throw new IllegalArgumentException("SFM program exceeds maximum length of " + MAX_PROGRAM_LENGTH + " characters");
        }

        BlockEntity blockEntity = manager.level().getBlockEntity(manager.position());
        try {
            SFMSupport.MANAGER_SET_PROGRAM.invoke(blockEntity, content);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalArgumentException("Failed to write SFM program", exception);
        }
    }

    @Override
    public void deleteFile(MinecraftServer server, ResolvedTarget target, String path) {
        requireManager(target);
        SFMPaths.normalize(path);
        throw new IllegalArgumentException("SFM programs cannot be deleted via VoxelLink; use in-game Reset on the manager");
    }

    public static String encodeTargetId(String localId) {
        return NAMESPACE + ":" + localId;
    }

    public static String encodeTargetId(net.minecraft.world.level.Level level, BlockPos position) {
        return encodeTargetId(PositionComputerReference.at(level, position).encode());
    }

    private static void tryAddManager(
            Map<BlockPos, TargetSummary> matches,
            BlockEntity blockEntity,
            ServerPlayer player,
            ServerLevel level
    ) {
        if (!SFMSupport.isManagerBlockEntity(blockEntity) || !hasDisk(blockEntity)) {
            return;
        }

        BlockPos position = blockEntity.getBlockPos();
        if (!canAccess(player, level, position)) {
            return;
        }

        String localId = PositionComputerReference.at(level, position).encode();
        matches.putIfAbsent(position, TargetSummary.of(encodeTargetId(localId), "", NAMESPACE, "manager"));
    }

    private static boolean canAccess(ServerPlayer player, ServerLevel level, BlockPos position) {
        if (player.serverLevel() != level) {
            return false;
        }

        return player.blockPosition().distSqr(position) <= (64.0 * 64.0);
    }

    private static boolean hasDisk(BlockEntity blockEntity) {
        try {
            ItemStack disk = (ItemStack) SFMSupport.MANAGER_GET_DISK.invoke(blockEntity);
            return disk != null && !disk.isEmpty();
        } catch (ReflectiveOperationException exception) {
            return false;
        }
    }

    private static SFMResolvedTarget requireManager(ResolvedTarget target) {
        if (!(target instanceof SFMResolvedTarget manager)) {
            throw new IllegalArgumentException("Expected SFM target");
        }
        return manager;
    }
}
