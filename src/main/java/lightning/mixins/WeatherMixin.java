/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lightning.Night
 *  lightning.modules.impl.visuals.AtmosphereModule
 *  net.minecraft.world.level.Level
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package lightning.mixins;

import lightning.Night;
import lightning.modules.impl.visuals.AtmosphereModule;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={Level.class})
public abstract class WeatherMixin {
    @Inject(method={"isRaining"}, at={@At(value="HEAD")}, cancellable=true)
    private void night$isRaining(CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        try {
            AtmosphereModule atmosphereModule = (AtmosphereModule)Night.MODULE_MANAGER.getModule(AtmosphereModule.class);
            if (atmosphereModule.isToggled() && !atmosphereModule.weather.getValue().equalsIgnoreCase("Unchanged")) {
                String weather = atmosphereModule.weather.getValue();
                callbackInfoReturnable.setReturnValue(weather.equalsIgnoreCase("Rain") || weather.equalsIgnoreCase("Thunder"));
            }
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }

    @Inject(method={"isThundering"}, at={@At(value="HEAD")}, cancellable=true)
    private void night$isThundering(CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        try {
            AtmosphereModule atmosphereModule = (AtmosphereModule)Night.MODULE_MANAGER.getModule(AtmosphereModule.class);
            if (!atmosphereModule.isToggled() || atmosphereModule.weather.getValue().equalsIgnoreCase("Unchanged")) {
                return;
            }
            callbackInfoReturnable.setReturnValue(atmosphereModule.weather.getValue().equalsIgnoreCase("Thunder"));
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }

    @Inject(method={"getRainLevel"}, at={@At(value="HEAD")}, cancellable=true)
    private void night$getRainLevel(float partialTick, CallbackInfoReturnable<Float> callbackInfoReturnable) {
        try {
            AtmosphereModule atmosphereModule = (AtmosphereModule)Night.MODULE_MANAGER.getModule(AtmosphereModule.class);
            if (!atmosphereModule.isToggled() || atmosphereModule.weather.getValue().equalsIgnoreCase("Unchanged")) {
                return;
            }
            String weather = atmosphereModule.weather.getValue();
            callbackInfoReturnable.setReturnValue(Float.valueOf(weather.equalsIgnoreCase("Rain") || weather.equalsIgnoreCase("Thunder") ? 1.0f : 0.0f));
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }

    @Inject(method={"getThunderLevel"}, at={@At(value="HEAD")}, cancellable=true)
    private void night$getThunderLevel(float partialTick, CallbackInfoReturnable<Float> callbackInfoReturnable) {
        try {
            AtmosphereModule atmosphereModule = (AtmosphereModule)Night.MODULE_MANAGER.getModule(AtmosphereModule.class);
            if (!atmosphereModule.isToggled() || atmosphereModule.weather.getValue().equalsIgnoreCase("Unchanged")) {
                return;
            }
            callbackInfoReturnable.setReturnValue(Float.valueOf(atmosphereModule.weather.getValue().equalsIgnoreCase("Thunder") ? 1.0f : 0.0f));
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }
}

