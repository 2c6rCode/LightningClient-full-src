/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.core.BlockPos
 */
package lightning.utils.miscellaneous;

import lightning.Night;
import lightning.modules.impl.core.RendersModule;
import lightning.utils.animations.Easing;
import lombok.Generated;
import net.minecraft.core.BlockPos;

public class RenderPosition {
    private BlockPos pos;
    private long startTime;

    public RenderPosition(BlockPos blockPos) {
        this.pos = blockPos;
        this.startTime = System.currentTimeMillis();
    }

    public boolean equals(Object object) {
        if (object instanceof RenderPosition) {
            return ((RenderPosition)object).pos.equals((Object)this.pos);
        }
        return false;
    }

    public float get() {
        return Easing.ease(1.0f - Easing.toDelta(this.startTime, Night.MODULE_MANAGER.getModule(RendersModule.class).duration.getValue().intValue()), Easing.Method.EASE_IN_CUBIC);
    }

    @Generated
    public void setPos(BlockPos blockPos) {
        this.pos = blockPos;
    }

    @Generated
    public void setStartTime(long l) {
        this.startTime = l;
    }

    @Generated
    public BlockPos getPos() {
        return this.pos;
    }

    @Generated
    public long getStartTime() {
        return this.startTime;
    }
}

