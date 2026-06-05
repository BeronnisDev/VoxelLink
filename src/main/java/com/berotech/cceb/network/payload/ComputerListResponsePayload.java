package com.berotech.cceb.network.payload;

import java.util.ArrayList;
import java.util.List;

import com.berotech.cceb.network.BridgePayloadTypes;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record ComputerListResponsePayload(int requestId, List<ComputerListEntry> computers, String errorMessage) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ComputerListResponsePayload> TYPE = BridgePayloadTypes.type("computer_list_response");

    public static final StreamCodec<FriendlyByteBuf, ComputerListResponsePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            ComputerListResponsePayload::requestId,
            ByteBufCodecs.collection(ArrayList::new, ComputerListEntry.STREAM_CODEC),
            ComputerListResponsePayload::computers,
            ByteBufCodecs.stringUtf8(2048),
            ComputerListResponsePayload::errorMessage,
            ComputerListResponsePayload::new
    );

    public boolean isSuccess() {
        return errorMessage == null || errorMessage.isEmpty();
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
