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

    public Animation(int duration, Easing.Method method) {
        this(0.0f, 0.0f, duration, method);
    }

    public Animation(float prev, float current, int duration, Easing.Method method) {
        this.prev = prev;
        this.current = current;
        this.duration = duration;
        this.activeDuration = duration;
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

    public float get(float target) {
        float currentValue = this.value();
        if (this.current != target) {
            float remaining = Math.abs(target - currentValue);
            float totalDistance = Math.abs(this.current - this.prev);
            float maxDistance = Math.max(totalDistance, remaining);
            int scaledDuration = maxDistance <= 1.0E-4f ? this.duration : Math.round((float)this.duration * Mth.clamp((float)(remaining / maxDistance), (float)0.0f, (float)1.0f));
            this.prev = currentValue;
            this.current = target;
            this.activeDuration = Math.max(40, scaledDuration);
            this.startTime = System.currentTimeMillis();
        }
        return currentValue;
    }

    public void setEasing(Easing.Method method) {
        if (this.easing == method) {
            return;
        }
        float currentValue = this.value();
        int elapsed = (int)Math.min(Easing.toDelta(this.startTime), Integer.MAX_VALUE);
        int remainingDuration = Math.max(0, this.activeDuration - elapsed);
        this.prev = currentValue;
        this.activeDuration = remainingDuration;
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
    public void setCurrent(float current) {
        this.current = current;
    }

    @Generated
    public void setPrev(float prev) {
        this.prev = prev;
    }

    @Generated
    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    @Generated
    public void setActiveDuration(int activeDuration) {
        this.activeDuration = activeDuration;
    }
}

