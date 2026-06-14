package com.berotech.voxellink.cc;

import java.util.List;

import com.berotech.voxellink.target.CCResolvedTarget;
import com.berotech.voxellink.target.ResolvedTarget;
import com.berotech.voxellink.target.TargetProvider;
import com.berotech.voxellink.target.TargetResolveResult;
import com.berotech.voxellink.target.TargetSummary;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.fml.ModList;

public final class CCProvider implements TargetProvider {
    public static final String NAMESPACE = "cc";

    @Override
    public String namespace() {
        return NAMESPACE;
    }

    @Override
    public boolean isAvailable() {
        return ModList.get().isLoaded("computercraft");
    }

    @Override
    public List<TargetSummary> listAccessible(MinecraftServer server, ServerPlayer player) {
        return CCComputerLookup.listAccessible(server, player).stream()
                .map(computer -> TargetSummary.of(
                        encodeTargetId(computer.id()),
                        computer.label(),
                        NAMESPACE,
                        "computer"
                ))
                .toList();
    }

    @Override
    public TargetResolveResult resolve(MinecraftServer server, ServerPlayer player, String localId) {
        return switch (CCComputerLookup.resolveDetailed(server, player, localId)) {
            case CCComputerLookup.ResolveResult.Success success ->
                    new TargetResolveResult.Success(new CCResolvedTarget(success.computer()));
            case CCComputerLookup.ResolveResult.Failure failure ->
                    new TargetResolveResult.Failure(failure.message());
        };
    }

    @Override
    public List<String> listFiles(MinecraftServer server, ResolvedTarget target, String path) {
        CCResolvedTarget computer = requireComputer(target);
        try {
            return CCFilesystemAccess.listFiles(server, computer.computer(), path);
        } catch (Exception exception) {
            throw new IllegalArgumentException(exception.getMessage(), exception);
        }
    }

    @Override
    public String readFile(MinecraftServer server, ResolvedTarget target, String path) {
        CCResolvedTarget computer = requireComputer(target);
        try {
            return CCFilesystemAccess.readFile(server, computer.computer(), path);
        } catch (Exception exception) {
            throw new IllegalArgumentException(exception.getMessage(), exception);
        }
    }

    @Override
    public void writeFile(MinecraftServer server, ResolvedTarget target, String path, String content) {
        CCResolvedTarget computer = requireComputer(target);
        try {
            CCFilesystemAccess.writeFile(server, computer.computer(), path, content);
        } catch (Exception exception) {
            throw new IllegalArgumentException(exception.getMessage(), exception);
        }
    }

    @Override
    public void deleteFile(MinecraftServer server, ResolvedTarget target, String path) {
        CCResolvedTarget computer = requireComputer(target);
        try {
            CCFilesystemAccess.deleteFile(server, computer.computer(), path);
        } catch (Exception exception) {
            throw new IllegalArgumentException(exception.getMessage(), exception);
        }
    }

    public static String encodeTargetId(String localId) {
        return NAMESPACE + ":" + localId;
    }

    private static CCResolvedTarget requireComputer(ResolvedTarget target) {
        if (!(target instanceof CCResolvedTarget computer)) {
            throw new IllegalArgumentException("Expected CC target");
        }
        return computer;
    }
}
