package com.berotech.cceb.client;

import com.berotech.cceb.Config;
import com.berotech.cceb.cc.CCComputerSupport;
import com.berotech.cceb.cc.LabelComputerReference;
import com.berotech.cceb.cc.PositionComputerReference;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public final class ComputerTargeting {
    private ComputerTargeting() {}

    public record TargetComputer(String id, String label, String positionId) {}

    public static TargetComputer getTargetComputer() {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        Level level = minecraft.level;
        if (player == null || level == null || !(minecraft.hitResult instanceof BlockHitResult blockHit) || blockHit.getType() != HitResult.Type.BLOCK) {
            return null;
        }

        BlockPos position = blockHit.getBlockPos();
        BlockEntity blockEntity = level.getBlockEntity(position);
        if (!CCComputerSupport.isComputerBlockEntity(blockEntity)) {
            return null;
        }

        String positionId = PositionComputerReference.at(level, position).encode();
        String label = readLabel(blockEntity);
        String preferredId = preferredId(label, positionId);
        return new TargetComputer(preferredId, label, positionId);
    }

    public static String getTargetComputerId() {
        TargetComputer target = getTargetComputer();
        return target == null ? null : target.id();
    }

    private static String preferredId(String label, String positionId) {
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
