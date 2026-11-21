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
    public void onMouseTurn(double d, double d2) {
        float f = (float)d2 * 0.15f;
        float f2 = (float)d * 0.15f;
        this.freePitch = Mth.clamp((float)(this.freePitch + f), (float)-90.0f, (float)90.0f);
        this.freeYaw += f2;
    }

    @SubscribeEvent
    public void onKeyboardTick(KeyboardTickEvent keyboardTickEvent) {
        if (FreecamModule.mc.player == null) {
            return;
        }
        this.prevFreeX = this.freeX;
        this.prevFreeY = this.freeY;
        this.prevFreeZ = this.freeZ;
        float f = keyboardTickEvent.getMovementForward();
        float f2 = keyboardTickEvent.getMovementSideways();
        double d = this.horizontalSpeed.getValue().doubleValue();
        if (f != 0.0f || f2 != 0.0f) {
            float f3 = (float)Math.sqrt(f * f + f2 * f2);
            if (f3 > 1.0E-4f && Math.abs(f3 - 1.0f) > 1.0E-4f) {
                f /= f3;
                f2 /= f3;
            }
            double d2 = Math.cos(Math.toRadians(this.freeYaw + 90.0f));
            double d3 = Math.sin(Math.toRadians(this.freeYaw + 90.0f));
            double d4 = (double)f * d * d2 + (double)f2 * d * d3;
            double d5 = (double)f * d * d3 - (double)f2 * d * d2;
            this.freeX += d4;
            this.freeZ += d5;
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
    public void setFreeYaw(float f) {
        this.freeYaw = f;
    }

    @Generated
    public void setFreePitch(float f) {
        this.freePitch = f;
    }

    @Generated
    public void setPrevFreeYaw(float f) {
        this.prevFreeYaw = f;
    }

    @Generated
    public void setPrevFreePitch(float f) {
        this.prevFreePitch = f;
    }

    @Generated
    public void setFreeX(double d) {
        this.freeX = d;
    }

    @Generated
    public void setFreeY(double d) {
        this.freeY = d;
    }

    @Generated
    public void setFreeZ(double d) {
        this.freeZ = d;
    }

    @Generated
    public void setPrevFreeX(double d) {
        this.prevFreeX = d;
    }

    @Generated
    public void setPrevFreeY(double d) {
        this.prevFreeY = d;
    }

    @Generated
    public void setPrevFreeZ(double d) {
        this.prevFreeZ = d;
    }

    @Generated
    public void setPrevCameraType(CameraType cameraType) {
        this.prevCameraType = cameraType;
    }
}

