package com.berotech.voxellink.network;

import com.berotech.voxellink.network.client.ClientResponseHandler;
import com.berotech.voxellink.network.payload.TargetListRequestPayload;
import com.berotech.voxellink.network.payload.TargetListResponsePayload;
import com.berotech.voxellink.network.payload.ErrorResponsePayload;
import com.berotech.voxellink.network.payload.FileDeleteRequestPayload;
import com.berotech.voxellink.network.payload.FileDeleteResponsePayload;
import com.berotech.voxellink.network.payload.FileEventPayload;
import com.berotech.voxellink.network.payload.FileListRequestPayload;
import com.berotech.voxellink.network.payload.FileListResponsePayload;
import com.berotech.voxellink.network.payload.FileReadRequestPayload;
import com.berotech.voxellink.network.payload.FileReadResponsePayload;
import com.berotech.voxellink.network.payload.FileWriteRequestPayload;
import com.berotech.voxellink.network.payload.FileWriteResponsePayload;
import com.berotech.voxellink.network.payload.OpenEditorPayload;
import com.berotech.voxellink.network.server.ServerPacketHandler;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class BridgeNetworking {
    public static final String PROTOCOL_VERSION = "1";

    private BridgeNetworking() {}

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(BridgeNetworking::registerPayloads);
    }

    private static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(PROTOCOL_VERSION);

        registrar.playToServer(FileListRequestPayload.TYPE, FileListRequestPayload.STREAM_CODEC, ServerPacketHandler::handleFileList);
        registrar.playToServer(FileReadRequestPayload.TYPE, FileReadRequestPayload.STREAM_CODEC, ServerPacketHandler::handleFileRead);
        registrar.playToServer(FileWriteRequestPayload.TYPE, FileWriteRequestPayload.STREAM_CODEC, ServerPacketHandler::handleFileWrite);
        registrar.playToServer(FileDeleteRequestPayload.TYPE, FileDeleteRequestPayload.STREAM_CODEC, ServerPacketHandler::handleFileDelete);
        registrar.playToServer(TargetListRequestPayload.TYPE, TargetListRequestPayload.STREAM_CODEC, ServerPacketHandler::handleTargetList);

        registrar.playToClient(FileListResponsePayload.TYPE, FileListResponsePayload.STREAM_CODEC, ClientResponseHandler::handleFileList);
        registrar.playToClient(FileReadResponsePayload.TYPE, FileReadResponsePayload.STREAM_CODEC, ClientResponseHandler::handleFileRead);
        registrar.playToClient(FileWriteResponsePayload.TYPE, FileWriteResponsePayload.STREAM_CODEC, ClientResponseHandler::handleFileWrite);
        registrar.playToClient(FileDeleteResponsePayload.TYPE, FileDeleteResponsePayload.STREAM_CODEC, ClientResponseHandler::handleFileDelete);
        registrar.playToClient(TargetListResponsePayload.TYPE, TargetListResponsePayload.STREAM_CODEC, ClientResponseHandler::handleTargetList);
        registrar.playToClient(ErrorResponsePayload.TYPE, ErrorResponsePayload.STREAM_CODEC, ClientResponseHandler::handleError);
        registrar.playToClient(FileEventPayload.TYPE, FileEventPayload.STREAM_CODEC, ClientResponseHandler::handleFileEvent);
        registrar.playToClient(OpenEditorPayload.TYPE, OpenEditorPayload.STREAM_CODEC, ClientResponseHandler::handleOpenEditor);
    }
}
