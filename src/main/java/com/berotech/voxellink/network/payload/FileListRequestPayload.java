package com.berotech.voxellink.network.payload;

import com.berotech.voxellink.network.BridgePayloadTypes;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record FileListRequestPayload(int requestId, String targetId, String path) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<FileListRequestPayload> TYPE = BridgePayloadTypes.type("file_list_request");

    public static final StreamCodec<FriendlyByteBuf, FileListRequestPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            FileListRequestPayload::requestId,
            ByteBufCodecs.stringUtf8(256),
            FileListRequestPayload::targetId,
            ByteBufCodecs.stringUtf8(1024),
            FileListRequestPayload::path,
            FileListRequestPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
