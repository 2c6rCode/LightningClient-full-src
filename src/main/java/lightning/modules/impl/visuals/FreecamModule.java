/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.client.CameraType
 *  net.minecraft.util.Mth
 */
package lightning.modules.impl.visuals;

import lightning.events.SubscribeEvent;
import lightning.events.impl.ClientConnectEvent;
import lightning.events.impl.KeyboardTickEvent;
import lightning.events.impl.PlayerDeathEvent;
import lightning.events.impl.TickEvent;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.modules.api.IFreecamModule;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.NumberSetting;
import lightning.utils.system.MathUtils;
import lombok.Generated;
import net.minecraft.client.CameraType;
import net.minecraft.util.Mth;

@RegisterModule(name="Freecam", description="Allows you to move your camera anywhere you want without restriction.", category=Module.Category.VISUALS)
public class FreecamModule
extends Module
implements IFreecamModule {
    public NumberSetting horizontalSpeed = new NumberSetting("HorizontalSpeed", "The speed at which your camera will move horizontally.", Float.valueOf(1.0f), Float.valueOf(0.1f), Float.valueOf(3.0f));
    public NumberSetting verticalSpeed = new NumberSetting("VerticalSpeed", "The speed at which your camera will move vertically.", Float.valueOf(0.5f), Float.valueOf(0.1f), Float.valueOf(3.0f));
    public BooleanSetting rotate = new BooleanSetting("Rotate", "Rotates your real player model along with the camera.", false);
    public BooleanSetting shift = new BooleanSetting("Shift", "Allows moving down with shift even when a GUI is open.", false);
    private float freeYaw;
    private float freePitch;
    private float prevFreeYaw;
    private float prevFreePitch;
    private double freeX;
    private double freeY;
    private double freeZ;
    private double prevFreeX;
    private double prevFreeY;
    private double prevFreeZ;
    private CameraType prevCameraType;

    @SubscribeEvent
    public void onTick(TickEvent tickEvent) {
        if (FreecamModule.mc.player == null) {
            return;
        }
        this.prevFreeYaw = this.freeYaw;
        this.prevFreePitch = this.freePitch;
        if (this.rotate.getValue()) {
            this.freeYaw = FreecamModule.mc.player.getYRot();
            this.freePitch = FreecamModule.mc.player.getXRot();
        }
    }

    @Override
    public void onMouseTurn(double deltaX, double deltaY) {
        float pitchDelta = (float)deltaY * 0.15f;
        float yawDelta = (float)deltaX * 0.15f;
        this.freePitch = Mth.clamp((float)(this.freePitch + pitchDelta), (float)-90.0f, (float)90.0f);
        this.freeYaw += yawDelta;
    }

    @SubscribeEvent
    public void onKeyboardTick(KeyboardTickEvent keyboardTickEvent) {
        if (FreecamModule.mc.player == null) {
            return;
        }
        this.prevFreeX = this.freeX;
        this.prevFreeY = this.freeY;
        this.prevFreeZ = this.freeZ;
        float forward = keyboardTickEvent.getMovementForward();
        float strafe = keyboardTickEvent.getMovementSideways();
        double speed = this.horizontalSpeed.getValue().doubleValue();
        if (forward != 0.0f || strafe != 0.0f) {
            float length = (float)Math.sqrt(forward * forward + strafe * strafe);
            if (length > 1.0E-4f && Math.abs(length - 1.0f) > 1.0E-4f) {
                forward /= length;
                strafe /= length;
            }
            double cosYaw = Math.cos(Math.toRadians(this.freeYaw + 90.0f));
            double sinYaw = Math.sin(Math.toRadians(this.freeYaw + 90.0f));
            double moveX = (double)forward * speed * cosYaw + (double)strafe * speed * sinYaw;
            double moveZ = (double)forward * speed * sinYaw - (double)strafe * speed * cosYaw;
            this.freeX += moveX;
            this.freeZ += moveZ;
        }
        if (FreecamModule.mc.options.keyJump.isDown()) {
            this.freeY += this.verticalSpeed.getValue().doubleValue();
        }
        if (FreecamModule.mc.options.keyShift.isDown() && (this.shift.getValue() || FreecamModule.mc.gui.screen() == null)) {
            this.freeY -= this.verticalSpeed.getValue().doubleValue();
        }
        keyboardTickEvent.setMovementForward(0.0f);
        keyboardTickEvent.setMovementSideways(0.0f);
        keyboardTickEvent.setCancelled(true);
    }

    @Override
    public void onEnable() {
        if (FreecamModule.mc.player == null || FreecamModule.mc.level == null) {
            this.setToggled(false);
            return;
        }
        FreecamModule.mc.smartCull = false;
        this.prevCameraType = FreecamModule.mc.options.getCameraType();
        FreecamModule.mc.options.setCameraType(CameraType.FIRST_PERSON);
        this.freeYaw = this.prevFreeYaw = FreecamModule.mc.player.getYRot();
        this.freePitch = this.prevFreePitch = FreecamModule.mc.player.getXRot();
        this.freeX = this.prevFreeX = FreecamModule.mc.player.getX();
        this.freeY = this.prevFreeY = FreecamModule.mc.player.getY() + (double)FreecamModule.mc.player.getEyeHeight(FreecamModule.mc.player.getPose());
        this.freeZ = this.prevFreeZ = FreecamModule.mc.player.getZ();
    }

    @SubscribeEvent
    public void onPlayerDeath(PlayerDeathEvent playerDeathEvent) {
        if (playerDeathEvent.getPlayer() == null || FreecamModule.mc.player == null) {
            return;
        }
        this.setToggled(false);
    }

    @SubscribeEvent
    public void onPlayerLogin(ClientConnectEvent clientConnectEvent) {
        this.setToggled(false);
    }

    @Override
    public void onDisable() {
        if (FreecamModule.mc.player == null || FreecamModule.mc.level == null) {
            return;
        }
        FreecamModule.mc.smartCull = true;
        if (this.prevCameraType != null) {
            FreecamModule.mc.options.setCameraType(this.prevCameraType);
        }
    }

    @Override
    public boolean isRotate() {
        return this.rotate.getValue();
    }

    @Override
    public float getFreeYaw() {
        return (float)MathUtils.interpolate(this.prevFreeYaw, this.freeYaw, mc.getDeltaTracker().getGameTimeDeltaPartialTick(true));
    }

    @Override
    public float getFreePitch() {
        return (float)MathUtils.interpolate(this.prevFreePitch, this.freePitch, mc.getDeltaTracker().getGameTimeDeltaPartialTick(true));
    }

    @Override
    public double getFreeX() {
        return MathUtils.interpolate(this.prevFreeX, this.freeX, mc.getDeltaTracker().getGameTimeDeltaPartialTick(true));
    }

    @Override
    public double getFreeY() {
        return MathUtils.interpolate(this.prevFreeY, this.freeY, mc.getDeltaTracker().getGameTimeDeltaPartialTick(true));
    }

    @Override
    public double getFreeZ() {
        return MathUtils.interpolate(this.prevFreeZ, this.freeZ, mc.getDeltaTracker().getGameTimeDeltaPartialTick(true));
    }

    @Generated
    public NumberSetting getHorizontalSpeed() {
        return this.horizontalSpeed;
    }

    @Generated
    public NumberSetting getVerticalSpeed() {
        return this.verticalSpeed;
    }

    @Generated
    public BooleanSetting getRotate() {
        return this.rotate;
    }

    @Generated
    public BooleanSetting getShift() {
        return this.shift;
    }

    @Generated
    public float getPrevFreeYaw() {
        return this.prevFreeYaw;
    }

    @Generated
    public float getPrevFreePitch() {
        return this.prevFreePitch;
    }

    @Generated
    public double getPrevFreeX() {
        return this.prevFreeX;
    }

    @Generated
    public double getPrevFreeY() {
        return this.prevFreeY;
    }

    @Generated
    public double getPrevFreeZ() {
        return this.prevFreeZ;
    }

    @Generated
    public CameraType getPrevCameraType() {
        return this.prevCameraType;
    }

    @Generated
    public void setHorizontalSpeed(NumberSetting numberSetting) {
        this.horizontalSpeed = numberSetting;
    }

    @Generated
    public void setVerticalSpeed(NumberSetting numberSetting) {
        this.verticalSpeed = numberSetting;
    }

    @Generated
    public void setRotate(BooleanSetting booleanSetting) {
        this.rotate = booleanSetting;
    }

    @Generated
    public void setShift(BooleanSetting booleanSetting) {
        this.shift = booleanSetting;
    }

    @Generated
    public void setFreeYaw(float freeYaw) {
        this.freeYaw = freeYaw;
    }

    @Generated
    public void setFreePitch(float freePitch) {
        this.freePitch = freePitch;
    }

    @Generated
    public void setPrevFreeYaw(float prevFreeYaw) {
        this.prevFreeYaw = prevFreeYaw;
    }

    @Generated
    public void setPrevFreePitch(float prevFreePitch) {
        this.prevFreePitch = prevFreePitch;
    }

    @Generated
    public void setFreeX(double freeX) {
        this.freeX = freeX;
    }

    @Generated
    public void setFreeY(double freeY) {
        this.freeY = freeY;
    }

    @Generated
    public void setFreeZ(double freeZ) {
        this.freeZ = freeZ;
    }

    @Generated
    public void setPrevFreeX(double prevFreeX) {
        this.prevFreeX = prevFreeX;
    }

    @Generated
    public void setPrevFreeY(double prevFreeY) {
        this.prevFreeY = prevFreeY;
    }

    @Generated
    public void setPrevFreeZ(double prevFreeZ) {
        this.prevFreeZ = prevFreeZ;
    }

    @Generated
    public void setPrevCameraType(CameraType cameraType) {
        this.prevCameraType = cameraType;
    }
}

