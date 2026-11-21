/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lightning.Night
 *  lightning.modules.impl.visuals.AtmosphereModule
 *  lightning.modules.impl.visuals.NoRenderModule
 *  net.minecraft.client.Camera
 *  net.minecraft.client.DeltaTracker
 *  net.minecraft.client.multiplayer.ClientLevel
 *  net.minecraft.client.renderer.fog.FogData
 *  net.minecraft.client.renderer.fog.environment.AtmosphericFogEnvironment
 *  net.minecraft.util.ARGB
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package lightning.mixins;

import java.awt.Color;
import lightning.Night;
import lightning.modules.impl.visuals.AtmosphereModule;
import lightning.modules.impl.visuals.NoRenderModule;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.environment.AtmosphericFogEnvironment;
import net.minecraft.util.ARGB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={AtmosphericFogEnvironment.class})
public class BackgroundRendererMixin {
    @Inject(method={"setupFog"}, at={@At(value="TAIL")})
    private void night$setupFog(FogData fogData, Camera camera, ClientLevel clientLevel, float f, DeltaTracker deltaTracker, CallbackInfo callbackInfo) {
        try {
            NoRenderModule noRenderModule = (NoRenderModule)Night.MODULE_MANAGER.getModule(NoRenderModule.class);
            AtmosphereModule atmosphereModule = (AtmosphereModule)Night.MODULE_MANAGER.getModule(AtmosphereModule.class);
            if (noRenderModule.isToggled() && noRenderModule.fog.getValue()) {
                fogData.environmentalStart = f * 4.0f;
                fogData.environmentalEnd = f * 4.25f;
            } else if (atmosphereModule != null && atmosphereModule.isToggled() && atmosphereModule.modifyFog.getValue()) {
                fogData.environmentalStart = atmosphereModule.fogStart.getValue().floatValue();
                fogData.environmentalEnd = atmosphereModule.fogEnd.getValue().floatValue();
            }
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }

    @Inject(method={"getBaseColor"}, at={@At(value="TAIL")}, cancellable=true)
    private void night$getBaseColor(ClientLevel clientLevel, Camera camera, int n, float f, CallbackInfoReturnable<Integer> callbackInfoReturnable) {
        try {
            AtmosphereModule atmosphereModule = (AtmosphereModule)Night.MODULE_MANAGER.getModule(AtmosphereModule.class);
            if (atmosphereModule != null && atmosphereModule.isToggled() && atmosphereModule.modifyFog.getValue()) {
                Color color = atmosphereModule.fogColor.getColor();
                callbackInfoReturnable.setReturnValue(ARGB.color(255, color.getRed(), color.getGreen(), color.getBlue()));
            }
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }
}

