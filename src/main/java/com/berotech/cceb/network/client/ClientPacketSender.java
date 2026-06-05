package com.berotech.cceb.network.client;

import java.util.concurrent.CompletableFuture;

import com.berotech.cceb.network.payload.FileDeleteRequestPayload;
import com.berotech.cceb.network.payload.FileDeleteResponsePayload;
import com.berotech.cceb.network.payload.FileListRequestPayload;
import com.berotech.cceb.network.payload.FileListResponsePayload;
import com.berotech.cceb.network.payload.FileReadRequestPayload;
import com.berotech.cceb.network.payload.FileReadResponsePayload;
import com.berotech.cceb.network.payload.FileWriteRequestPayload;
import com.berotech.cceb.network.payload.FileWriteResponsePayload;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.network.PacketDistributor;

public final class ClientPacketSender {
    private static final long REQUEST_TIMEOUT_SECONDS = 10;

    private ClientPacketSender() {}

    public static CompletableFuture<FileListResponsePayload> sendFileListRequest(String computerId, String path) {
        int requestId = PendingRequests.nextId();
        CompletableFuture<FileListResponsePayload> future = PendingRequests.register(requestId, REQUEST_TIMEOUT_SECONDS);
        send(new FileListRequestPayload(requestId, computerId, path));
        return future;
    }

    public static CompletableFuture<FileReadResponsePayload> sendFileReadRequest(String computerId, String path) {
        int requestId = PendingRequests.nextId();
        CompletableFuture<FileReadResponsePayload> future = PendingRequests.register(requestId, REQUEST_TIMEOUT_SECONDS);
        send(new FileReadRequestPayload(requestId, computerId, path));
        return future;
    }

    public static CompletableFuture<FileWriteResponsePayload> sendFileWriteRequest(String computerId, String path, String content) {
        int requestId = PendingRequests.nextId();
        CompletableFuture<FileWriteResponsePayload> future = PendingRequests.register(requestId, REQUEST_TIMEOUT_SECONDS);
        send(new FileWriteRequestPayload(requestId, computerId, path, content));
        return future;
    }

    public static CompletableFuture<FileDeleteResponsePayload> sendFileDeleteRequest(String computerId, String path) {
        int requestId = PendingRequests.nextId();
        CompletableFuture<FileDeleteResponsePayload> future = PendingRequests.register(requestId, REQUEST_TIMEOUT_SECONDS);
        send(new FileDeleteRequestPayload(requestId, computerId, path));
        return future;
    }

    private static void send(CustomPacketPayload payload) {
        if (Minecraft.getInstance().getConnection() == null) {
            throw new IllegalStateException("Cannot send bridge packet while not connected to a world");
        }
        PacketDistributor.sendToServer(payload);
    }
}
