package com.berotech.cceb.network.payload;

import com.berotech.cceb.network.BridgePayloadTypes;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record FileWriteRequestPayload(int requestId, String computerId, String path, String content) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<FileWriteRequestPayload> TYPE = BridgePayloadTypes.type("file_write_request");

    public static final StreamCodec<FriendlyByteBuf, FileWriteRequestPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            FileWriteRequestPayload::requestId,
            ByteBufCodecs.stringUtf8(256),
            FileWriteRequestPayload::computerId,
            ByteBufCodecs.stringUtf8(1024),
            FileWriteRequestPayload::path,
            ByteBufCodecs.stringUtf8(65535),
            FileWriteRequestPayload::content,
            FileWriteRequestPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
