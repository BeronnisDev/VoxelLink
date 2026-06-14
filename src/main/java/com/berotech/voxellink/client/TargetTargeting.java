package com.berotech.voxellink.client;

import com.berotech.voxellink.Config;
import com.berotech.voxellink.cc.CCComputerSupport;
import com.berotech.voxellink.cc.CCProvider;
import com.berotech.voxellink.cc.LabelComputerReference;
import com.berotech.voxellink.cc.PositionComputerReference;
import com.berotech.voxellink.sfm.SFMProvider;
import com.berotech.voxellink.sfm.SFMSupport;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.fml.ModList;

public final class TargetTargeting {
    private TargetTargeting() {}

    public record Target(String id, String label, String positionId, String backend, String kind) {}

    public static Target getTarget() {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        Level level = minecraft.level;
        if (player == null || level == null || !(minecraft.hitResult instanceof BlockHitResult blockHit) || blockHit.getType() != HitResult.Type.BLOCK) {
            return null;
        }

        BlockPos position = blockHit.getBlockPos();
        BlockEntity blockEntity = level.getBlockEntity(position);
        if (blockEntity == null) {
            return null;
        }

        if (ModList.get().isLoaded("computercraft") && CCComputerSupport.isComputerBlockEntity(blockEntity)) {
            String localPositionId = PositionComputerReference.at(level, position).encode();
            String label = readLabel(blockEntity);
            String localPreferredId = preferredLocalId(label, localPositionId);
            return new Target(
                    CCProvider.encodeTargetId(localPreferredId),
                    label,
                    CCProvider.encodeTargetId(localPositionId),
                    CCProvider.NAMESPACE,
                    "computer"
            );
        }

        if (ModList.get().isLoaded("sfm") && SFMSupport.isManagerBlockEntity(blockEntity)) {
            String encoded = SFMProvider.encodeTargetId(level, position);
            return new Target(encoded, "", encoded, SFMProvider.NAMESPACE, "manager");
        }

        return null;
    }

    public static String getTargetId() {
        Target target = getTarget();
        return target == null ? null : target.id();
    }

    private static String preferredLocalId(String label, String positionId) {
        if (Config.PREFER_LABEL_IDS.get() && label != null && !label.isBlank()) {
            return LabelComputerReference.of(label).encode();
        }
        return positionId;
    }

    private static String readLabel(BlockEntity blockEntity) {
        try {
            return CCComputerSupport.getLabel(blockEntity);
        } catch (ReflectiveOperationException exception) {
            return null;
        }
    }
}
