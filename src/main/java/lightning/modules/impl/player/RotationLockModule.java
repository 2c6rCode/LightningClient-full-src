/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lightning.mixins.accessors.EntityAccessor
 *  net.minecraft.util.Mth
 */
package lightning.modules.impl.player;

import lightning.events.SubscribeEvent;
import lightning.events.impl.PlayerUpdateEvent;
import lightning.mixins.accessors.EntityAccessor;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.ModeSetting;
import lightning.settings.impl.NumberSetting;
import net.minecraft.util.Mth;

@RegisterModule(name="RotationLock", description="Locks your rotation to a certain yaw and pitch.", category=Module.Category.PLAYER)
public class RotationLockModule
extends Module {
    public ModeSetting mode = new ModeSetting("Mode", "Whether the pitch or yaw will be locked.", "Both", new String[]{"Yaw", "Pitch", "Both"});
    public BooleanSetting custom = new BooleanSetting("Custom", "Holds your locked angle steady (camera free to turn) and only re-locks, snapping straight to the next 45deg, once you've turned far enough past it -- instead of an always-fixed value.", new ModeSetting.Visibility(this.mode, "Yaw", "Pitch", "Both"), false);
    public NumberSetting pitch = new NumberSetting("Pitch", "Pitch", "The fixed degrees pitch locks to when Custom is off.", new ModeSetting.Visibility(this.mode, "Pitch", "Both"), Float.valueOf(0.0f), Float.valueOf(-90.0f), Float.valueOf(90.0f), 1);
    private boolean lockInitialized = false;
    private float lockedYaw = 0.0f;
    private float lockedPitch = 0.0f;
    private static final long SHAKE_DURATION_MS = 180L;
    private static final float SHAKE_AMPLITUDE_DEG = 3.0f;
    private long yawSnapAtMs = -1L;
    private long pitchSnapAtMs = -1L;

    @Override
    public void onEnable() {
        this.lockInitialized = false;
    }

    private float shakeOffset(long snapTimeMs) {
        if (snapTimeMs < 0L) {
            return 0.0f;
        }
        long elapsedMs = System.currentTimeMillis() - snapTimeMs;
        if (elapsedMs >= 180L) {
            return 0.0f;
        }
        float progress = (float)elapsedMs / 180.0f;
        return (float)(Math.sin((double)progress * Math.PI * 5.0) * 3.0 * (double)(1.0f - progress));
    }

    public float getShakeYawOffset() {
        return this.custom.getValue() ? this.shakeOffset(this.yawSnapAtMs) : 0.0f;
    }

    public float getShakePitchOffset() {
        return this.custom.getValue() ? this.shakeOffset(this.pitchSnapAtMs) : 0.0f;
    }

    @SubscribeEvent
    public void onPlayerUpdate(PlayerUpdateEvent playerUpdateEvent) {
        float snappedAngle;
        if (RotationLockModule.mc.player == null || !this.custom.getValue()) {
            this.lockInitialized = false;
            return;
        }
        float rawYaw = ((EntityAccessor)RotationLockModule.mc.player).getRawYRot();
        float rawPitch = ((EntityAccessor)RotationLockModule.mc.player).getRawXRot();
        if (!this.lockInitialized) {
            this.lockedYaw = RotationLockModule.nearest45(rawYaw);
            this.lockedPitch = RotationLockModule.nearest45(rawPitch);
            this.lockInitialized = true;
            return;
        }
        if (Math.abs(Mth.wrapDegrees((float)(rawYaw - this.lockedYaw))) >= 45.0f) {
            snappedAngle = RotationLockModule.nearest45(rawYaw);
            if (snappedAngle != this.lockedYaw) {
                this.yawSnapAtMs = System.currentTimeMillis();
            }
            this.lockedYaw = snappedAngle;
        }
        if (Math.abs(Mth.wrapDegrees((float)(rawPitch - this.lockedPitch))) >= 45.0f) {
            snappedAngle = RotationLockModule.nearest45(rawPitch);
            if (snappedAngle != this.lockedPitch) {
                this.pitchSnapAtMs = System.currentTimeMillis();
            }
            this.lockedPitch = snappedAngle;
        }
    }

    private static float nearest45(float angle) {
        return Mth.wrapDegrees((float)((float)Math.round(angle / 45.0f) * 45.0f));
    }

    public float getYawValue(float currentYaw) {
        if (this.custom.getValue()) {
            return this.lockedYaw;
        }
        return RotationLockModule.nearest45(currentYaw);
    }

    public float getPitchValue(float currentPitch) {
        if (this.custom.getValue()) {
            return this.lockedPitch;
        }
        return this.pitch.getValue().floatValue();
    }

    @Override
    public String getMetaData() {
        String yawText = this.custom.getValue() ? String.valueOf(this.lockedYaw) : "Auto";
        String pitchText = this.custom.getValue() ? String.valueOf(this.lockedPitch) : String.valueOf(this.pitch.getValue().floatValue());
        return yawText + ", " + pitchText;
    }
}

