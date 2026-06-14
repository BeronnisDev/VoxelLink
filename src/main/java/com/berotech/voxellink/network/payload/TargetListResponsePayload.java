package com.berotech.voxellink.network.payload;

import java.util.ArrayList;
import java.util.List;

import com.berotech.voxellink.network.BridgePayloadTypes;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record TargetListResponsePayload(int requestId, List<TargetListEntry> targets, String errorMessage) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<TargetListResponsePayload> TYPE = BridgePayloadTypes.type("target_list_response");

    public static final StreamCodec<FriendlyByteBuf, TargetListResponsePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            TargetListResponsePayload::requestId,
            ByteBufCodecs.collection(ArrayList::new, TargetListEntry.STREAM_CODEC),
            TargetListResponsePayload::targets,
            ByteBufCodecs.stringUtf8(2048),
            TargetListResponsePayload::errorMessage,
            TargetListResponsePayload::new
    );

    public boolean isSuccess() {
        return errorMessage == null || errorMessage.isEmpty();
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
