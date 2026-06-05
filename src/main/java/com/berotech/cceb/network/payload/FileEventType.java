package com.berotech.cceb.network.payload;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public enum FileEventType {
    CREATED,
    MODIFIED,
    DELETED;

    public static final StreamCodec<FriendlyByteBuf, FileEventType> STREAM_CODEC = StreamCodec.of(
            FriendlyByteBuf::writeEnum,
            buf -> buf.readEnum(FileEventType.class)
    );
}
