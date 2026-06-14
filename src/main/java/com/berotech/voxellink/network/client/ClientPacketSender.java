package com.berotech.voxellink.network.client;

import java.util.concurrent.CompletableFuture;

import com.berotech.voxellink.network.payload.TargetListRequestPayload;
import com.berotech.voxellink.network.payload.TargetListResponsePayload;
import com.berotech.voxellink.network.payload.FileDeleteRequestPayload;
import com.berotech.voxellink.network.payload.FileDeleteResponsePayload;
import com.berotech.voxellink.network.payload.FileListRequestPayload;
import com.berotech.voxellink.network.payload.FileListResponsePayload;
import com.berotech.voxellink.network.payload.FileReadRequestPayload;
import com.berotech.voxellink.network.payload.FileReadResponsePayload;
import com.berotech.voxellink.network.payload.FileWriteRequestPayload;
import com.berotech.voxellink.network.payload.FileWriteResponsePayload;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.network.PacketDistributor;

public final class ClientPacketSender {
    private static final long REQUEST_TIMEOUT_SECONDS = 10;

    private ClientPacketSender() {}

    public static CompletableFuture<FileListResponsePayload> sendFileListRequest(String targetId, String path) {
        int requestId = PendingRequests.nextId();
        CompletableFuture<FileListResponsePayload> future = PendingRequests.register(requestId, REQUEST_TIMEOUT_SECONDS);
        send(new FileListRequestPayload(requestId, targetId, path));
        return future;
    }

    public static CompletableFuture<FileReadResponsePayload> sendFileReadRequest(String targetId, String path) {
        int requestId = PendingRequests.nextId();
        CompletableFuture<FileReadResponsePayload> future = PendingRequests.register(requestId, REQUEST_TIMEOUT_SECONDS);
        send(new FileReadRequestPayload(requestId, targetId, path));
        return future;
    }

    public static CompletableFuture<FileWriteResponsePayload> sendFileWriteRequest(String targetId, String path, String content) {
        int requestId = PendingRequests.nextId();
        CompletableFuture<FileWriteResponsePayload> future = PendingRequests.register(requestId, REQUEST_TIMEOUT_SECONDS);
        send(new FileWriteRequestPayload(requestId, targetId, path, content));
        return future;
    }

    public static CompletableFuture<FileDeleteResponsePayload> sendFileDeleteRequest(String targetId, String path) {
        int requestId = PendingRequests.nextId();
        CompletableFuture<FileDeleteResponsePayload> future = PendingRequests.register(requestId, REQUEST_TIMEOUT_SECONDS);
        send(new FileDeleteRequestPayload(requestId, targetId, path));
        return future;
    }

    public static CompletableFuture<TargetListResponsePayload> sendTargetListRequest() {
        int requestId = PendingRequests.nextId();
        CompletableFuture<TargetListResponsePayload> future = PendingRequests.register(requestId, REQUEST_TIMEOUT_SECONDS);
        send(new TargetListRequestPayload(requestId));
        return future;
    }

    private static void send(CustomPacketPayload payload) {
        if (Minecraft.getInstance().getConnection() == null) {
            throw new IllegalStateException("Cannot send bridge packet while not connected to a world");
        }
        PacketDistributor.sendToServer(payload);
    }
}
