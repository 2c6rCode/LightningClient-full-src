/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.world.entity.Entity
 */
package lightning.utils.rotations;

import lombok.Generated;
import net.minecraft.world.entity.Entity;

public class Rotation {
    private float yaw;
    private float pitch;

    public Rotation(float f, float f2) {
        this.yaw = f;
        this.pitch = f2;
    }

    public Rotation(Entity entity) {
        this(entity.getYRot(), entity.getXRot());
    }

    public void apply(Entity entity) {
        entity.setYRot(this.yaw);
        entity.setXRot(this.pitch);
    }

    @Generated
    public float getYaw() {
        return this.yaw;
    }

    @Generated
    public float getPitch() {
        return this.pitch;
    }

    @Generated
    public void setYaw(float f) {
        this.yaw = f;
    }

    @Generated
    public void setPitch(float f) {
        this.pitch = f;
    }
}

