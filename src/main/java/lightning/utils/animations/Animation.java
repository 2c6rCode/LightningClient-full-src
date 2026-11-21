/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.util.Mth
 */
package lightning.utils.animations;

import lightning.utils.animations.Easing;
import lombok.Generated;
import net.minecraft.util.Mth;

public class Animation {
    private static final int MIN_DURATION_MS = 40;
    private float current;
    private float prev;
    private long startTime;
    private Easing.Method easing;
    private final int duration;
    private int activeDuration;

    public Animation(int n, Easing.Method method) {
        this(0.0f, 0.0f, n, method);
    }

    public Animation(float f, float f2, int n, Easing.Method method) {
        this.prev = f;
        this.current = f2;
        this.duration = n;
        this.activeDuration = n;
        this.easing = method;
        this.startTime = System.currentTimeMillis();
    }

    public float value() {
        if (this.activeDuration <= 0) {
            return this.current;
        }
        return Mth.lerp((float)Easing.ease(Easing.toDelta(this.startTime, this.activeDuration), this.easing), (float)this.prev, (float)this.current);
    }

    public boolean isFinished() {
        return this.activeDuration <= 0 || Easing.toDelta(this.startTime, this.activeDuration) >= 1.0f;
    }

    public float get() {
        return this.value();
    }

    public float get(float f) {
        float f2 = this.value();
        if (this.current != f) {
            float f3 = Math.abs(f - f2);
            float f4 = Math.abs(this.current - this.prev);
            float f5 = Math.max(f4, f3);
            int n = f5 <= 1.0E-4f ? this.duration : Math.round((float)this.duration * Mth.clamp((float)(f3 / f5), (float)0.0f, (float)1.0f));
            this.prev = f2;
            this.current = f;
            this.activeDuration = Math.max(40, n);
            this.startTime = System.currentTimeMillis();
        }
        return f2;
    }

    public void setEasing(Easing.Method method) {
        if (this.easing == method) {
            return;
        }
        float f = this.value();
        int n = (int)Math.min(Easing.toDelta(this.startTime), Integer.MAX_VALUE);
        int n2 = Math.max(0, this.activeDuration - n);
        this.prev = f;
        this.activeDuration = n2;
        this.startTime = System.currentTimeMillis();
        this.easing = method;
    }

    @Generated
    public float getCurrent() {
        return this.current;
    }

    @Generated
    public float getPrev() {
        return this.prev;
    }

    @Generated
    public long getStartTime() {
        return this.startTime;
    }

    @Generated
    public Easing.Method getEasing() {
        return this.easing;
    }

    @Generated
    public int getDuration() {
        return this.duration;
    }

    @Generated
    public int getActiveDuration() {
        return this.activeDuration;
    }

    @Generated
    public void setCurrent(float f) {
        this.current = f;
    }

    @Generated
    public void setPrev(float f) {
        this.prev = f;
    }

    @Generated
    public void setStartTime(long l) {
        this.startTime = l;
    }

    @Generated
    public void setActiveDuration(int n) {
        this.activeDuration = n;
    }
}

