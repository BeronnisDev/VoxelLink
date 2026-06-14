package com.berotech.voxellink.sfm;

import com.berotech.voxellink.network.payload.OpenEditorPayload;
import com.berotech.voxellink.target.SFMResolvedTarget;
import com.berotech.voxellink.target.TargetProviderRegistry;
import com.berotech.voxellink.target.TargetResolveResult;

import net.minecraft.server.level.ServerPlayer;

public final class SFMEditorOpenRequests {
    private SFMEditorOpenRequests() {}

    public record Result(boolean success, String message) {}

    public static Result requestOpen(ServerPlayer player, String targetId) {
        TargetResolveResult resolve = TargetProviderRegistry.resolve(player.getServer(), player, targetId);
        if (!(resolve instanceof TargetResolveResult.Success success)) {
            String message = resolve instanceof TargetResolveResult.Failure failure
                    ? failure.message()
                    : "Cannot access target '" + targetId + "'";
            return new Result(false, message);
        }

        if (!(success.target() instanceof SFMResolvedTarget)) {
            return new Result(false, "Target is not an SFM manager");
        }

        player.connection.send(new OpenEditorPayload(targetId, SFMPaths.PROGRAM_FILE));
        return new Result(true, null);
    }
}
