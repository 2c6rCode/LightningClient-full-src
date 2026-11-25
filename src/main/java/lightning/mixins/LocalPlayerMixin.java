/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lightning.Night
 *  lightning.modules.impl.player.RotationLockModule
 *  lightning.utils.IMinecraft
 *  net.minecraft.client.player.LocalPlayer
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package lightning.mixins;

import lightning.Night;
import lightning.mixins.accessors.EntityAccessor;
import lightning.modules.impl.player.RotationLockModule;
import lightning.utils.IMinecraft;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={LocalPlayer.class})
public abstract class LocalPlayerMixin
implements IMinecraft {
    @Inject(method={"getViewYRot"}, at={@At(value="HEAD")}, cancellable=true)
    private void night$unlockViewYaw(float partialTick, CallbackInfoReturnable<Float> callbackInfoReturnable) {
        try {
            if (((LocalPlayer)(Object)this).isPassenger()) {
                return;
            }
            RotationLockModule rotationLockModule = (RotationLockModule)Night.MODULE_MANAGER.getModule(RotationLockModule.class);
            if (rotationLockModule.isToggled() && rotationLockModule.custom.getValue() && (rotationLockModule.mode.getValue().equals("Yaw") || rotationLockModule.mode.getValue().equals("Both"))) {
                callbackInfoReturnable.setReturnValue(Float.valueOf(((EntityAccessor)((Object)this)).getRawYRot()));
            }
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }

    @Inject(method={"getViewXRot"}, at={@At(value="HEAD")}, cancellable=true)
    private void night$unlockViewPitch(float partialTick, CallbackInfoReturnable<Float> callbackInfoReturnable) {
        try {
            RotationLockModule rotationLockModule = (RotationLockModule)Night.MODULE_MANAGER.getModule(RotationLockModule.class);
            if (rotationLockModule.isToggled() && rotationLockModule.custom.getValue() && (rotationLockModule.mode.getValue().equals("Pitch") || rotationLockModule.mode.getValue().equals("Both"))) {
                callbackInfoReturnable.setReturnValue(Float.valueOf(((EntityAccessor)((Object)this)).getRawXRot()));
            }
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }
}

