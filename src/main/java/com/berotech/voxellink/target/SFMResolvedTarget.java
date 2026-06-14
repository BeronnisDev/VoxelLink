package com.berotech.voxellink.target;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

public record SFMResolvedTarget(ServerLevel level, BlockPos position) implements ResolvedTarget {}
