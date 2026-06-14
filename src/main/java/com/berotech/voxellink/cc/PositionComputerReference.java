package com.berotech.voxellink.cc;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public record PositionComputerReference(ResourceLocation dimension, BlockPos position) implements ComputerReference {
    public static final String PREFIX = "pos:";

    public static PositionComputerReference at(Level level, BlockPos position) {
        return new PositionComputerReference(level.dimension().location(), position.immutable());
    }

    @Override
    public String encode() {
        return PREFIX + dimension + ":" + position.getX() + ":" + position.getY() + ":" + position.getZ();
    }

    public static PositionComputerReference parse(String computerId) {
        if (computerId == null || !computerId.startsWith(PREFIX)) {
            throw new IllegalArgumentException("Computer id must start with '" + PREFIX + "'");
        }

        String remainder = computerId.substring(PREFIX.length());
        int lastColon = remainder.lastIndexOf(':');
        if (lastColon <= 0) {
            throw new IllegalArgumentException("Invalid computer id: " + computerId);
        }

        int secondLastColon = remainder.lastIndexOf(':', lastColon - 1);
        int thirdLastColon = remainder.lastIndexOf(':', secondLastColon - 1);
        if (thirdLastColon <= 0) {
            throw new IllegalArgumentException("Invalid computer id: " + computerId);
        }

        String dimensionPart = remainder.substring(0, thirdLastColon);
        int x = parseCoordinate(remainder.substring(thirdLastColon + 1, secondLastColon), computerId);
        int y = parseCoordinate(remainder.substring(secondLastColon + 1, lastColon), computerId);
        int z = parseCoordinate(remainder.substring(lastColon + 1), computerId);

        return new PositionComputerReference(ResourceLocation.parse(dimensionPart), new BlockPos(x, y, z));
    }

    public ResourceKey<Level> dimensionKey() {
        return ResourceKey.create(Registries.DIMENSION, dimension);
    }

    private static int parseCoordinate(String value, String computerId) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Invalid computer id: " + computerId, exception);
        }
    }
}
