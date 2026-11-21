/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.core.BlockPos
 */
package lightning.events.impl;

import lightning.events.Event;
import lombok.Generated;
import net.minecraft.core.BlockPos;

public class BreakBlockEvent
extends Event {
    private final BlockPos pos;

    @Generated
    public BlockPos getPos() {
        return this.pos;
    }

    @Generated
    public BreakBlockEvent(BlockPos blockPos) {
        this.pos = blockPos;
    }
}

