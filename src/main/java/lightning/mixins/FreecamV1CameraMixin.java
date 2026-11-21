/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lightning.Night
 *  lightning.modules.impl.visuals.FreecamV1Module
 *  net.minecraft.client.Camera
 *  net.minecraft.client.DeltaTracker
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.ModifyArgs
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.invoke.arg.Args
 */
package lightning.mixins;

import lightning.Night;
import lightning.modules.impl.visuals.FreecamV1Module;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(value={Camera.class})
public abstract class FreecamV1CameraMixin {
    @Shadow
    private boolean detached;

    private static FreecamV1Module night$freecamV1() {
        if (Night.MODULE_MANAGER == null) {
            return null;
        }
        FreecamV1Module freecamV1Module = (FreecamV1Module)Night.MODULE_MANAGER.getModule(FreecamV1Module.class);
        return freecamV1Module != null && freecamV1Module.isToggled() ? freecamV1Module : null;
    }

    @Inject(method={"update"}, at={@At(value="TAIL")})
    private void night$freecamV1$update(DeltaTracker deltaTracker, CallbackInfo callbackInfo) {
        if (FreecamV1CameraMixin.night$freecamV1() != null) {
            this.detached = true;
        }
    }

    @ModifyArgs(method={"alignWithEntity"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/Camera;setRotation(FF)V")})
    private void night$freecamV1$setRotation(Args args) {
        try {
            FreecamV1Module freecamV1Module = FreecamV1CameraMixin.night$freecamV1();
            if (freecamV1Module != null) {
                args.setAll(new Object[]{Float.valueOf(freecamV1Module.getFakeYaw()), Float.valueOf(freecamV1Module.getFakePitch())});
            }
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }

    @ModifyArgs(method={"alignWithEntity"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/Camera;setPosition(DDD)V")})
    private void night$freecamV1$setPos(Args args) {
        try {
            FreecamV1Module freecamV1Module = FreecamV1CameraMixin.night$freecamV1();
            if (freecamV1Module != null) {
                args.setAll(new Object[]{freecamV1Module.getFakeX(), freecamV1Module.getFakeY(), freecamV1Module.getFakeZ()});
            }
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }
}

