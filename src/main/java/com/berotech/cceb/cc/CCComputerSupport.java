package com.berotech.cceb.cc;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import net.minecraft.world.level.block.entity.BlockEntity;

public final class CCComputerSupport {
    public static final String COMPUTER_BLOCK_ENTITY = "dan200.computercraft.shared.computer.blocks.AbstractComputerBlockEntity";

    public static final Method GET_COMPUTER_ID;
    public static final Method GET_LABEL;
    public static final Method IS_USABLE;
    public static final Field STORAGE_CAPACITY;

    static {
        try {
            Class<?> computerBlockEntity = Class.forName(COMPUTER_BLOCK_ENTITY);
            GET_COMPUTER_ID = computerBlockEntity.getMethod("getComputerID");
            GET_LABEL = computerBlockEntity.getMethod("getLabel");
            IS_USABLE = computerBlockEntity.getMethod("isUsable", net.minecraft.world.entity.player.Player.class);
            STORAGE_CAPACITY = computerBlockEntity.getDeclaredField("storageCapacity");
            STORAGE_CAPACITY.setAccessible(true);
        } catch (ReflectiveOperationException exception) {
            throw new ExceptionInInitializerError(exception);
        }
    }

    private CCComputerSupport() {}

    public static boolean isComputerBlockEntity(BlockEntity blockEntity) {
        if (blockEntity == null) {
            return false;
        }

        Class<?> type = blockEntity.getClass();
        while (type != null) {
            if (COMPUTER_BLOCK_ENTITY.equals(type.getName())) {
                return true;
            }
            type = type.getSuperclass();
        }
        return false;
    }

    public static int getComputerId(BlockEntity blockEntity) throws ReflectiveOperationException {
        return (int) GET_COMPUTER_ID.invoke(blockEntity);
    }

    public static String getLabel(BlockEntity blockEntity) throws ReflectiveOperationException {
        return (String) GET_LABEL.invoke(blockEntity);
    }

    public static boolean isUsable(BlockEntity blockEntity, net.minecraft.world.entity.player.Player player) throws ReflectiveOperationException {
        return (boolean) IS_USABLE.invoke(blockEntity, player);
    }

    public static long getStorageCapacity(BlockEntity blockEntity) throws ReflectiveOperationException {
        return (long) STORAGE_CAPACITY.get(blockEntity);
    }
}
