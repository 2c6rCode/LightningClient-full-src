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

public class DestroyBlockEvent
extends Event {
    private final BlockPos position;

    @Generated
    public DestroyBlockEvent(BlockPos blockPos) {
        this.position = blockPos;
    }

    @Generated
    public BlockPos getPosition() {
        return this.position;
    }
}

