package com.berotech.cceb.network.payload;

import com.berotech.cceb.network.BridgePayloadTypes;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record ComputerListRequestPayload(int requestId) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ComputerListRequestPayload> TYPE = BridgePayloadTypes.type("computer_list_request");

    public static final StreamCodec<FriendlyByteBuf, ComputerListRequestPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            ComputerListRequestPayload::requestId,
            ComputerListRequestPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
