package com.berotech.cceb.network.payload;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record ComputerListEntry(String id, String label) {
    public static final StreamCodec<FriendlyByteBuf, ComputerListEntry> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.stringUtf8(256),
            ComputerListEntry::id,
            ByteBufCodecs.stringUtf8(256),
            ComputerListEntry::label,
            ComputerListEntry::new
    );
}
