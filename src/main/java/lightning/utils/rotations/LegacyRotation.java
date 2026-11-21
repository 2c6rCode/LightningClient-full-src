/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package lightning.utils.rotations;

import lightning.modules.Module;
import lombok.Generated;

public class LegacyRotation {
    private float yaw;
    private float pitch;
    private final Module module;
    private final int priority;
    private long time;

    public LegacyRotation(float f, float f2, int n) {
        this.yaw = f;
        this.pitch = f2;
        this.module = null;
        this.priority = n;
        this.time = System.currentTimeMillis();
    }

    public LegacyRotation(float f, float f2, Module module, int n) {
        this.yaw = f;
        this.pitch = f2;
        this.module = module;
        this.priority = n;
        this.time = System.currentTimeMillis();
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
    public Module getModule() {
        return this.module;
    }

    @Generated
    public int getPriority() {
        return this.priority;
    }

    @Generated
    public long getTime() {
        return this.time;
    }

    @Generated
    public void setYaw(float f) {
        this.yaw = f;
    }

    @Generated
    public void setPitch(float f) {
        this.pitch = f;
    }

    @Generated
    public void setTime(long l) {
        this.time = l;
    }
}

