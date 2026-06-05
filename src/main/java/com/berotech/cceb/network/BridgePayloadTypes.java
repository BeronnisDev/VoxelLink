package com.berotech.cceb.network;

import com.berotech.cceb.CCEditorBridge;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public final class BridgePayloadTypes {
    private BridgePayloadTypes() {}

    public static <T extends CustomPacketPayload> CustomPacketPayload.Type<T> type(String path) {
        return new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(CCEditorBridge.MODID, path));
    }
}
