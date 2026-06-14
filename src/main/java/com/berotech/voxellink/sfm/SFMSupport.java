package com.berotech.voxellink.sfm;

import java.lang.reflect.Method;

import net.minecraft.world.level.block.entity.BlockEntity;

public final class SFMSupport {
    public static final String MANAGER_BLOCK_ENTITY = "ca.teamdman.sfm.common.blockentity.ManagerBlockEntity";
    public static final String DISK_ITEM = "ca.teamdman.sfm.common.item.DiskItem";

    public static final Method MANAGER_GET_DISK;
    public static final Method MANAGER_GET_PROGRAM_STRING;
    public static final Method MANAGER_SET_PROGRAM;
    public static final Method DISK_GET_PROGRAM_STRING;
    public static final Method DISK_SET_PROGRAM;

    static {
        try {
            Class<?> managerBlockEntity = Class.forName(MANAGER_BLOCK_ENTITY);
            Class<?> diskItem = Class.forName(DISK_ITEM);

            MANAGER_GET_DISK = managerBlockEntity.getMethod("getDisk");
            MANAGER_GET_PROGRAM_STRING = managerBlockEntity.getMethod("getProgramString");
            MANAGER_SET_PROGRAM = managerBlockEntity.getMethod("setProgram", String.class);
            DISK_GET_PROGRAM_STRING = diskItem.getMethod("getProgramString", net.minecraft.world.item.ItemStack.class);
            DISK_SET_PROGRAM = diskItem.getMethod("setProgram", net.minecraft.world.item.ItemStack.class, String.class);
        } catch (ReflectiveOperationException exception) {
            throw new ExceptionInInitializerError(exception);
        }
    }

    private SFMSupport() {}

    public static boolean isManagerBlockEntity(BlockEntity blockEntity) {
        if (blockEntity == null) {
            return false;
        }

        Class<?> type = blockEntity.getClass();
        while (type != null) {
            if (MANAGER_BLOCK_ENTITY.equals(type.getName())) {
                return true;
            }
            type = type.getSuperclass();
        }
        return false;
    }
}
