package com.berotech.cceb.network.payload;

import com.berotech.cceb.network.BridgePayloadTypes;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record FileDeleteRequestPayload(int requestId, String computerId, String path) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<FileDeleteRequestPayload> TYPE = BridgePayloadTypes.type("file_delete_request");

    public static final StreamCodec<FriendlyByteBuf, FileDeleteRequestPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            FileDeleteRequestPayload::requestId,
            ByteBufCodecs.stringUtf8(256),
            FileDeleteRequestPayload::computerId,
            ByteBufCodecs.stringUtf8(1024),
            FileDeleteRequestPayload::path,
            FileDeleteRequestPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
