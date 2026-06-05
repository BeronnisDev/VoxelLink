package com.berotech.cceb.network.payload;

import com.berotech.cceb.network.BridgePayloadTypes;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record FileWriteResponsePayload(int requestId, String errorMessage) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<FileWriteResponsePayload> TYPE = BridgePayloadTypes.type("file_write_response");

    public static final StreamCodec<FriendlyByteBuf, FileWriteResponsePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            FileWriteResponsePayload::requestId,
            ByteBufCodecs.stringUtf8(2048),
            FileWriteResponsePayload::errorMessage,
            FileWriteResponsePayload::new
    );

    public boolean isSuccess() {
        return errorMessage == null || errorMessage.isEmpty();
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
