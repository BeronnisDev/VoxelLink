package com.berotech.voxellink.network.payload;

import com.berotech.voxellink.network.BridgePayloadTypes;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record TargetListRequestPayload(int requestId) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<TargetListRequestPayload> TYPE = BridgePayloadTypes.type("computer_list_request");

    public static final StreamCodec<FriendlyByteBuf, TargetListRequestPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            TargetListRequestPayload::requestId,
            TargetListRequestPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
