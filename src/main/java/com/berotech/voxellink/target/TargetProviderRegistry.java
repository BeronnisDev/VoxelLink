package com.berotech.voxellink.target;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.berotech.voxellink.cc.CCProvider;
import com.berotech.voxellink.sfm.SFMProvider;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public final class TargetProviderRegistry {
    private static final List<TargetProvider> PROVIDERS = buildProviders();

    private static List<TargetProvider> buildProviders() {
        List<TargetProvider> providers = new ArrayList<>();
        if (net.neoforged.fml.ModList.get().isLoaded("computercraft")) {
            providers.add(new CCProvider());
        }
        if (net.neoforged.fml.ModList.get().isLoaded("sfm")) {
            providers.add(new SFMProvider());
        }
        return List.copyOf(providers);
    }

    private TargetProviderRegistry() {}

    public static List<String> availableBackends() {
        List<String> backends = new ArrayList<>();
        for (TargetProvider provider : PROVIDERS) {
            if (provider.isAvailable()) {
                backends.add(provider.namespace());
            }
        }
        return backends;
    }

    public static Optional<TargetProvider> forNamespace(String namespace) {
        return PROVIDERS.stream()
                .filter(provider -> provider.namespace().equals(namespace) && provider.isAvailable())
                .findFirst();
    }

    public static Optional<TargetProvider> forTargetId(String targetId) {
        try {
            TargetId parsed = TargetId.parse(targetId);
            return forNamespace(parsed.namespace());
        } catch (IllegalArgumentException exception) {
            return Optional.empty();
        }
    }

    public static TargetResolveResult resolve(MinecraftServer server, ServerPlayer player, String targetId) {
        TargetId parsed;
        try {
            parsed = TargetId.parse(targetId);
        } catch (IllegalArgumentException exception) {
            return new TargetResolveResult.Failure(exception.getMessage());
        }

        Optional<TargetProvider> provider = forNamespace(parsed.namespace());
        if (provider.isEmpty()) {
            if (availableBackends().isEmpty()) {
                return new TargetResolveResult.Failure("No script backends are available (install CC: Tweaked or Super Factory Manager)");
            }
            return new TargetResolveResult.Failure("Unknown or unavailable backend '" + parsed.namespace() + "'");
        }

        return provider.get().resolve(server, player, parsed.localId());
    }

    public static List<TargetSummary> listAccessible(MinecraftServer server, ServerPlayer player) {
        List<TargetSummary> targets = new ArrayList<>();
        for (TargetProvider provider : PROVIDERS) {
            if (provider.isAvailable()) {
                targets.addAll(provider.listAccessible(server, player));
            }
        }
        return targets;
    }
}
