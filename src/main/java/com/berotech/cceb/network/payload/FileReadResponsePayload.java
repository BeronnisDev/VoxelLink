package com.berotech.cceb.network.payload;

import com.berotech.cceb.network.BridgePayloadTypes;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record FileReadResponsePayload(int requestId, String content, String errorMessage) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<FileReadResponsePayload> TYPE = BridgePayloadTypes.type("file_read_response");

    public static final StreamCodec<FriendlyByteBuf, FileReadResponsePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            FileReadResponsePayload::requestId,
            ByteBufCodecs.stringUtf8(65535),
            FileReadResponsePayload::content,
            ByteBufCodecs.stringUtf8(2048),
            FileReadResponsePayload::errorMessage,
            FileReadResponsePayload::new
    );

    public boolean isSuccess() {
        return errorMessage == null || errorMessage.isEmpty();
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
