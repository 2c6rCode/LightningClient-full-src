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

public class PlayerMineEvent
extends Event {
    private final int actorID;
    private final BlockPos position;

    @Generated
    public int getActorID() {
        return this.actorID;
    }

    @Generated
    public BlockPos getPosition() {
        return this.position;
    }

    @Generated
    public PlayerMineEvent(int n, BlockPos blockPos) {
        this.actorID = n;
        this.position = blockPos;
    }
}

