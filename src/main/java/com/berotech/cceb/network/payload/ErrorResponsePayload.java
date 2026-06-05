package com.berotech.cceb.network.payload;

import com.berotech.cceb.network.BridgePayloadTypes;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record ErrorResponsePayload(int requestId, String message) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ErrorResponsePayload> TYPE = BridgePayloadTypes.type("error_response");

    public static final StreamCodec<FriendlyByteBuf, ErrorResponsePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            ErrorResponsePayload::requestId,
            ByteBufCodecs.stringUtf8(2048),
            ErrorResponsePayload::message,
            ErrorResponsePayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
