package com.berotech.voxellink.network.payload;

import com.berotech.voxellink.network.BridgePayloadTypes;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record RequestOpenEditorPayload(String targetId) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<RequestOpenEditorPayload> TYPE = BridgePayloadTypes.type("request_open_editor");

    public static final StreamCodec<FriendlyByteBuf, RequestOpenEditorPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.stringUtf8(256),
            RequestOpenEditorPayload::targetId,
            RequestOpenEditorPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
