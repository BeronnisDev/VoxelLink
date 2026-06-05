package com.berotech.cceb.network.payload;

import com.berotech.cceb.network.BridgePayloadTypes;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record FileReadRequestPayload(int requestId, String computerId, String path) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<FileReadRequestPayload> TYPE = BridgePayloadTypes.type("file_read_request");

    public static final StreamCodec<FriendlyByteBuf, FileReadRequestPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            FileReadRequestPayload::requestId,
            ByteBufCodecs.stringUtf8(256),
            FileReadRequestPayload::computerId,
            ByteBufCodecs.stringUtf8(1024),
            FileReadRequestPayload::path,
            FileReadRequestPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
