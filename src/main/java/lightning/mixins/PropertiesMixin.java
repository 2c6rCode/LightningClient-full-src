/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lightning.Night
 *  lightning.modules.impl.visuals.AtmosphereModule
 *  net.minecraft.client.ClientClockManager
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package lightning.mixins;

import lightning.Night;
import lightning.modules.impl.visuals.AtmosphereModule;
import net.minecraft.client.ClientClockManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={ClientClockManager.class})
public class PropertiesMixin {
    @Inject(method={"getTotalTicks"}, at={@At(value="HEAD")}, cancellable=true)
    private void getTotalTicks(CallbackInfoReturnable<Long> callbackInfoReturnable) {
        try {
            AtmosphereModule atmosphereModule = Night.MODULE_MANAGER != null ? (AtmosphereModule)Night.MODULE_MANAGER.getModule(AtmosphereModule.class) : null;
            AtmosphereModule atmosphereModule2 = atmosphereModule;
            if (atmosphereModule != null && atmosphereModule.isToggled() && atmosphereModule.modifyTime.getValue()) {
                callbackInfoReturnable.setReturnValue(atmosphereModule.time.getValue().longValue() * 100L);
            }
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }
}

