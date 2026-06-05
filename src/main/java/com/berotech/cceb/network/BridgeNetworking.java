package com.berotech.cceb.network;

import com.berotech.cceb.network.client.ClientResponseHandler;
import com.berotech.cceb.network.payload.ErrorResponsePayload;
import com.berotech.cceb.network.payload.FileDeleteRequestPayload;
import com.berotech.cceb.network.payload.FileDeleteResponsePayload;
import com.berotech.cceb.network.payload.FileEventPayload;
import com.berotech.cceb.network.payload.FileListRequestPayload;
import com.berotech.cceb.network.payload.FileListResponsePayload;
import com.berotech.cceb.network.payload.FileReadRequestPayload;
import com.berotech.cceb.network.payload.FileReadResponsePayload;
import com.berotech.cceb.network.payload.FileWriteRequestPayload;
import com.berotech.cceb.network.payload.FileWriteResponsePayload;
import com.berotech.cceb.network.server.ServerPacketHandler;

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

        registrar.playToClient(FileListResponsePayload.TYPE, FileListResponsePayload.STREAM_CODEC, ClientResponseHandler::handleFileList);
        registrar.playToClient(FileReadResponsePayload.TYPE, FileReadResponsePayload.STREAM_CODEC, ClientResponseHandler::handleFileRead);
        registrar.playToClient(FileWriteResponsePayload.TYPE, FileWriteResponsePayload.STREAM_CODEC, ClientResponseHandler::handleFileWrite);
        registrar.playToClient(FileDeleteResponsePayload.TYPE, FileDeleteResponsePayload.STREAM_CODEC, ClientResponseHandler::handleFileDelete);
        registrar.playToClient(ErrorResponsePayload.TYPE, ErrorResponsePayload.STREAM_CODEC, ClientResponseHandler::handleError);
        registrar.playToClient(FileEventPayload.TYPE, FileEventPayload.STREAM_CODEC, ClientResponseHandler::handleFileEvent);
    }
}
