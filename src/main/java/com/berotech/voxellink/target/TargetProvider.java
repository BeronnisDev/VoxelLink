package com.berotech.voxellink.target;

import java.util.List;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public interface TargetProvider {
    String namespace();

    boolean isAvailable();

    List<TargetSummary> listAccessible(MinecraftServer server, ServerPlayer player);

    TargetResolveResult resolve(MinecraftServer server, ServerPlayer player, String localId);

    List<String> listFiles(MinecraftServer server, ResolvedTarget target, String path);

    String readFile(MinecraftServer server, ResolvedTarget target, String path);

    void writeFile(MinecraftServer server, ResolvedTarget target, String path, String content);

    void deleteFile(MinecraftServer server, ResolvedTarget target, String path);
}
