package com.berotech.voxellink.network;

import com.berotech.voxellink.VoxelLink;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public final class BridgePayloadTypes {
    private BridgePayloadTypes() {}

    public static <T extends CustomPacketPayload> CustomPacketPayload.Type<T> type(String path) {
        return new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(VoxelLink.MODID, path));
    }
}
