package com.berotech.cceb.network.payload;

import com.berotech.cceb.network.BridgePayloadTypes;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record OpenEditorPayload(String computerId, String path) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<OpenEditorPayload> TYPE = BridgePayloadTypes.type("open_editor");

    public static final StreamCodec<FriendlyByteBuf, OpenEditorPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.stringUtf8(256),
            OpenEditorPayload::computerId,
            ByteBufCodecs.stringUtf8(1024),
            OpenEditorPayload::path,
            OpenEditorPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
