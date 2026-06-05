package com.berotech.cceb.network.server;

import com.berotech.cceb.network.payload.FileEventPayload;
import com.berotech.cceb.network.payload.FileEventType;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class FileEventEmitter {
    private FileEventEmitter() {}

    public static void sendToPlayer(IPayloadContext context, FileEventType eventType, String computerId, String path) {
        if (!(context.player() instanceof ServerPlayer player)) {
            return;
        }
        player.connection.send(new FileEventPayload(eventType, computerId, path));
    }
}
