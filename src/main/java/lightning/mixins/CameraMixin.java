/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lightning.Night
 *  lightning.modules.api.IFOVModifierModule
 *  lightning.modules.api.IFreecamModule
 *  lightning.modules.api.IMotionCameraModule
 *  lightning.modules.api.IViewClipModule
 *  lightning.modules.impl.combat.AutoMaceModule
 *  lightning.modules.impl.movement.ElytraFlyModule
 *  lightning.modules.impl.player.RotationLockModule
 *  lightning.modules.impl.visuals.NoRenderModule
 *  lightning.utils.IMinecraft
 *  net.minecraft.client.Camera
 *  net.minecraft.client.DeltaTracker
 *  net.minecraft.world.level.material.FogType
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.ModifyArgs
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 *  org.spongepowered.asm.mixin.injection.invoke.arg.Args
 */
package lightning.mixins;

import lightning.Night;
import lightning.modules.api.IFOVModifierModule;
import lightning.modules.api.IFreecamModule;
import lightning.modules.api.IMotionCameraModule;
import lightning.modules.api.IViewClipModule;
import lightning.modules.impl.combat.AutoMaceModule;
import lightning.modules.impl.movement.ElytraFlyModule;
import lightning.modules.impl.player.RotationLockModule;
import lightning.modules.impl.visuals.NoRenderModule;
import lightning.utils.IMinecraft;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.world.level.material.FogType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(value={Camera.class})
public abstract class CameraMixin
implements IMinecraft {
    @Shadow
    private boolean detached;

    @Shadow
    protected abstract void move(float zoom, float dy, float dx);

    @Shadow
    protected abstract float getMaxZoom(float maxZoom);

    @ModifyArgs(method={"alignWithEntity"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/Camera;getMaxZoom(F)F")})
    private void update(Args args) {
        try {
            IMotionCameraModule iMotionCameraModule = Night.MODULE_MANAGER != null ? (IMotionCameraModule)Night.MODULE_MANAGER.getModule("MotionCamera") : null;
            IMotionCameraModule iMotionCameraModule2 = iMotionCameraModule;
            if (iMotionCameraModule != null && iMotionCameraModule.isToggled() && iMotionCameraModule.isSmoothPerspective()) {
                args.set(0, (Object)Float.valueOf((float)iMotionCameraModule.getDistance()));
                return;
            }
            IViewClipModule iViewClipModule = Night.MODULE_MANAGER != null ? (IViewClipModule)Night.MODULE_MANAGER.getModule("ViewClip") : null;
            IViewClipModule iViewClipModule2 = iViewClipModule;
            if (iViewClipModule != null && iViewClipModule.isToggled() && iViewClipModule.isExtend()) {
                args.set(0, (Object)Float.valueOf(iViewClipModule.getDistanceValue()));
            }
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }

    @Inject(method={"getMaxZoom"}, at={@At(value="HEAD")}, cancellable=true)
    private void clipToSpace(float maxZoom, CallbackInfoReturnable<Float> callbackInfoReturnable) {
        try {
            IViewClipModule iViewClipModule = Night.MODULE_MANAGER != null ? (IViewClipModule)Night.MODULE_MANAGER.getModule("ViewClip") : null;
            IViewClipModule unusedViewClipModule = iViewClipModule;
            if (iViewClipModule != null && iViewClipModule.isToggled()) {
                callbackInfoReturnable.setReturnValue(Float.valueOf(maxZoom));
            }
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }

    @Inject(method={"getFluidInCamera"}, at={@At(value="HEAD")}, cancellable=true)
    private void getSubmersionType(CallbackInfoReturnable<FogType> callbackInfoReturnable) {
        try {
            if (Night.MODULE_MANAGER != null && ((NoRenderModule)Night.MODULE_MANAGER.getModule(NoRenderModule.class)).isToggled() && ((NoRenderModule)Night.MODULE_MANAGER.getModule(NoRenderModule.class)).liquidOverlay.getValue()) {
                callbackInfoReturnable.setReturnValue(FogType.NONE);
            }
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }

    @Inject(method={"update"}, at={@At(value="TAIL")})
    private void update$TAIL(DeltaTracker deltaTracker, CallbackInfo callbackInfo) {
        try {
            if (Night.MODULE_MANAGER == null) {
                return;
            }
            if (((IFreecamModule)Night.MODULE_MANAGER.getModule("Freecam")).isToggled()) {
                this.detached = true;
                return;
            }
            IMotionCameraModule iMotionCameraModule = (IMotionCameraModule)Night.MODULE_MANAGER.getModule("MotionCamera");
            if (iMotionCameraModule != null && iMotionCameraModule.isToggled() && iMotionCameraModule.isSmoothPerspective() && iMotionCameraModule.shouldBeDetached()) {
                this.detached = true;
                if (CameraMixin.mc.options != null && CameraMixin.mc.options.getCameraType().isFirstPerson()) {
                    float distance = (float)iMotionCameraModule.getDistance();
                    if (distance > 0.001f) {
                        this.move(-this.getMaxZoom(distance), 0.0f, 0.0f);
                    }
                }
            }
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }

    @ModifyArgs(method={"alignWithEntity"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/Camera;setRotation(FF)V")})
    private void update$setRotation(Args args) {
        try {
            if (Night.MODULE_MANAGER != null) {
                IFreecamModule iFreecamModule = (IFreecamModule)Night.MODULE_MANAGER.getModule("Freecam");
                if (iFreecamModule != null && iFreecamModule.isToggled()) {
                    args.setAll(new Object[]{Float.valueOf(iFreecamModule.getFreeYaw()), Float.valueOf(iFreecamModule.getFreePitch())});
                    return;
                }
                ElytraFlyModule elytraFlyModule = (ElytraFlyModule)Night.MODULE_MANAGER.getModule(ElytraFlyModule.class);
                if (elytraFlyModule != null && elytraFlyModule.isFreeLookActive()) {
                    boolean mirrored = CameraMixin.mc.options != null && CameraMixin.mc.options.getCameraType().isMirrored();
                    float yaw = elytraFlyModule.getFreeYaw();
                    float pitch = elytraFlyModule.getFreePitch();
                    if (mirrored) {
                        yaw += 180.0f;
                        pitch = -pitch;
                    }
                    args.setAll(new Object[]{Float.valueOf(yaw), Float.valueOf(pitch)});
                    return;
                }
                AutoMaceModule autoMaceModule = (AutoMaceModule)Night.MODULE_MANAGER.getModule(AutoMaceModule.class);
                if (autoMaceModule != null && autoMaceModule.isFreeLookActive()) {
                    boolean mirrored = CameraMixin.mc.options != null && CameraMixin.mc.options.getCameraType().isMirrored();
                    float yaw = autoMaceModule.getFreeYaw();
                    float pitch = autoMaceModule.getFreePitch();
                    if (mirrored) {
                        yaw += 180.0f;
                        pitch = -pitch;
                    }
                    args.setAll(new Object[]{Float.valueOf(yaw), Float.valueOf(pitch)});
                    return;
                }
                RotationLockModule rotationLockModule = (RotationLockModule)Night.MODULE_MANAGER.getModule(RotationLockModule.class);
                if (rotationLockModule != null && rotationLockModule.isToggled() && rotationLockModule.custom.getValue()) {
                    float shakeYaw = rotationLockModule.getShakeYawOffset();
                    float shakePitch = rotationLockModule.getShakePitchOffset();
                    if (shakeYaw != 0.0f || shakePitch != 0.0f) {
                        args.setAll(new Object[]{Float.valueOf(((Float)args.get(0)).floatValue() + shakeYaw), Float.valueOf(((Float)args.get(1)).floatValue() + shakePitch)});
                    }
                }
            }
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }

    @ModifyArgs(method={"alignWithEntity"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/Camera;setPosition(DDD)V")})
    private void update$setPos(Args args) {
        try {
            if (Night.MODULE_MANAGER == null) {
                return;
            }
            IFreecamModule iFreecamModule = (IFreecamModule)Night.MODULE_MANAGER.getModule("Freecam");
            if (iFreecamModule.isToggled()) {
                args.setAll(new Object[]{iFreecamModule.getFreeX(), iFreecamModule.getFreeY(), iFreecamModule.getFreeZ()});
            } else {
                IMotionCameraModule iMotionCameraModule = (IMotionCameraModule)Night.MODULE_MANAGER.getModule("MotionCamera");
                if (iMotionCameraModule != null && iMotionCameraModule.isToggled() && iMotionCameraModule.on()) {
                    args.setAll(new Object[]{iMotionCameraModule.getFakeX(), iMotionCameraModule.getFakeY(), iMotionCameraModule.getFakeZ()});
                }
            }
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }

    @Inject(method={"calculateFov"}, at={@At(value="TAIL")}, cancellable=true)
    private void getFOV(float fov, CallbackInfoReturnable<Float> callbackInfoReturnable) {
        try {
            if (Night.MODULE_MANAGER == null) {
                return;
            }
            IFOVModifierModule iFOVModifierModule = (IFOVModifierModule)Night.MODULE_MANAGER.getModule("FOVModifier");
            if (iFOVModifierModule.isToggled()) {
                callbackInfoReturnable.setReturnValue(Float.valueOf(iFOVModifierModule.getFovValue()));
            }
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }
}

