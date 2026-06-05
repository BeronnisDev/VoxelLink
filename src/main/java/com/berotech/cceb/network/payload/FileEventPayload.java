package com.berotech.cceb.network.payload;

import com.berotech.cceb.network.BridgePayloadTypes;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record FileEventPayload(FileEventType eventType, String computerId, String path) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<FileEventPayload> TYPE = BridgePayloadTypes.type("file_event");

    public static final StreamCodec<FriendlyByteBuf, FileEventPayload> STREAM_CODEC = StreamCodec.composite(
            FileEventType.STREAM_CODEC,
            FileEventPayload::eventType,
            ByteBufCodecs.stringUtf8(256),
            FileEventPayload::computerId,
            ByteBufCodecs.stringUtf8(1024),
            FileEventPayload::path,
            FileEventPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
