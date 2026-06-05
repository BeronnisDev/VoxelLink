package com.berotech.cceb.network.payload;

import com.berotech.cceb.network.BridgePayloadTypes;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record FileDeleteResponsePayload(int requestId, String errorMessage) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<FileDeleteResponsePayload> TYPE = BridgePayloadTypes.type("file_delete_response");

    public static final StreamCodec<FriendlyByteBuf, FileDeleteResponsePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            FileDeleteResponsePayload::requestId,
            ByteBufCodecs.stringUtf8(2048),
            FileDeleteResponsePayload::errorMessage,
            FileDeleteResponsePayload::new
    );

    public boolean isSuccess() {
        return errorMessage == null || errorMessage.isEmpty();
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
