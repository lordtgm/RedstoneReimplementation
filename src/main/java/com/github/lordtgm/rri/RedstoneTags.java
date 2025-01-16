package com.github.lordtgm.rri;

import net.minestom.server.tag.Tag;

public interface RedstoneTags {
    Tag<Byte> StrongPowerLevel = Tag.Byte("StrongPowerLevel").defaultValue((byte) 0);
    Tag<Byte> WeakPowerLevel = Tag.Byte("WeakPowerLevel").defaultValue((byte) 0);
}
