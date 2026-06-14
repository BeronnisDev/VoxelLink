package com.berotech.voxellink.network.payload;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record TargetListEntry(String id, String label, String backend, String kind) {
    public static final StreamCodec<FriendlyByteBuf, TargetListEntry> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.stringUtf8(256),
            TargetListEntry::id,
            ByteBufCodecs.stringUtf8(256),
            TargetListEntry::label,
            ByteBufCodecs.stringUtf8(32),
            TargetListEntry::backend,
            ByteBufCodecs.stringUtf8(32),
            TargetListEntry::kind,
            TargetListEntry::new
    );
}
