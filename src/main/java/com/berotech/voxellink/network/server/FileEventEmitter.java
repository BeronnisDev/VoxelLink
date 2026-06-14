package com.berotech.voxellink.network.server;

import com.berotech.voxellink.network.payload.FileEventPayload;
import com.berotech.voxellink.network.payload.FileEventType;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class FileEventEmitter {
    private FileEventEmitter() {}

    public static void sendToPlayer(IPayloadContext context, FileEventType eventType, String targetId, String path) {
        if (!(context.player() instanceof ServerPlayer player)) {
            return;
        }
        player.connection.send(new FileEventPayload(eventType, targetId, path));
    }
}
