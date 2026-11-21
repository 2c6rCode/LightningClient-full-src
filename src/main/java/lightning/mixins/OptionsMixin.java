/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lightning.Night
 *  lightning.modules.api.IFreecamModule
 *  net.minecraft.client.CameraType
 *  net.minecraft.client.Options
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package lightning.mixins;

import lightning.Night;
import lightning.modules.api.IFreecamModule;
import net.minecraft.client.CameraType;
import net.minecraft.client.Options;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={Options.class})
public abstract class OptionsMixin {
    @Inject(method={"setCameraType"}, at={@At(value="HEAD")}, cancellable=true)
    private void night$blockThirdPersonOnFreecam(CameraType cameraType, CallbackInfo callbackInfo) {
        try {
            IFreecamModule iFreecamModule;
            if (Night.MODULE_MANAGER != null && (iFreecamModule = (IFreecamModule)Night.MODULE_MANAGER.getModule("Freecam")) != null && iFreecamModule.isToggled() && !cameraType.isFirstPerson()) {
                callbackInfo.cancel();
            }
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }
}

