package com.berotech.voxellink.network.payload;

import java.util.ArrayList;
import java.util.List;

import com.berotech.voxellink.network.BridgePayloadTypes;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record FileListResponsePayload(int requestId, List<String> files, String errorMessage) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<FileListResponsePayload> TYPE = BridgePayloadTypes.type("file_list_response");

    public static final StreamCodec<FriendlyByteBuf, FileListResponsePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            FileListResponsePayload::requestId,
            ByteBufCodecs.collection(ArrayList::new, ByteBufCodecs.stringUtf8(1024)),
            FileListResponsePayload::files,
            ByteBufCodecs.stringUtf8(2048),
            FileListResponsePayload::errorMessage,
            FileListResponsePayload::new
    );

    public boolean isSuccess() {
        return errorMessage == null || errorMessage.isEmpty();
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
