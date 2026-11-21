/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lightning.Night
 *  lightning.modules.api.IAmbienceModule
 *  lightning.modules.api.IFullBrightModule
 *  lightning.modules.impl.visuals.NoRenderModule
 *  net.minecraft.client.renderer.Lightmap
 *  net.minecraft.client.renderer.state.LightmapRenderState
 *  org.joml.Vector3f
 *  org.joml.Vector3fc
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package lightning.mixins;

import java.awt.Color;
import lightning.Night;
import lightning.modules.api.IAmbienceModule;
import lightning.modules.api.IFullBrightModule;
import lightning.modules.impl.visuals.NoRenderModule;
import net.minecraft.client.renderer.Lightmap;
import net.minecraft.client.renderer.state.LightmapRenderState;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={Lightmap.class})
public class LightmapTextureManagerMixin {
    @Inject(method={"render"}, at={@At(value="HEAD")})
    private void night$render(LightmapRenderState lightmapRenderState, CallbackInfo callbackInfo) {
        try {
            NoRenderModule noRenderModule;
            IFullBrightModule iFullBrightModule = (IFullBrightModule)Night.MODULE_MANAGER.getModule("FullBright");
            if (iFullBrightModule.isToggled() && iFullBrightModule.getModeValue().equalsIgnoreCase("Gamma")) {
                lightmapRenderState.blockFactor = 1.0f;
                lightmapRenderState.skyFactor = 1.0f;
                lightmapRenderState.brightness = 1.0f;
                lightmapRenderState.nightVisionEffectIntensity = 1.0f;
                lightmapRenderState.darknessEffectScale = 0.0f;
                lightmapRenderState.bossOverlayWorldDarkening = 0.0f;
                lightmapRenderState.needsUpdate = true;
            }
            if ((noRenderModule = (NoRenderModule)Night.MODULE_MANAGER.getModule(NoRenderModule.class)).isToggled() && noRenderModule.blindness.getValue()) {
                lightmapRenderState.darknessEffectScale = 0.0f;
                lightmapRenderState.needsUpdate = true;
            }
            IAmbienceModule iAmbienceModule = Night.MODULE_MANAGER != null ? (IAmbienceModule)Night.MODULE_MANAGER.getModule("Ambience") : null;
            IAmbienceModule iAmbienceModule2 = iAmbienceModule;
            if (iAmbienceModule != null && iAmbienceModule.isToggled()) {
                Color color = iAmbienceModule.getColor();
                float f = (float)color.getRed() / 255.0f;
                float f2 = (float)color.getGreen() / 255.0f;
                float f3 = (float)color.getBlue() / 255.0f;
                float f4 = (float)color.getAlpha() / 255.0f;
                if (iAmbienceModule.isLightmapEnabled()) {
                    Vector3fc vector3fc;
                    if (iAmbienceModule.isSkyEnabled()) {
                        vector3fc = lightmapRenderState.skyLightColor != null ? lightmapRenderState.skyLightColor : new Vector3f(1.0f, 1.0f, 1.0f);
                        lightmapRenderState.skyLightColor = new Vector3f(vector3fc.x() * (1.0f - f4) + f * f4, vector3fc.y() * (1.0f - f4) + f2 * f4, vector3fc.z() * (1.0f - f4) + f3 * f4);
                        Vector3fc vector3fc2 = lightmapRenderState.ambientColor != null ? lightmapRenderState.ambientColor : new Vector3f(0.0f, 0.0f, 0.0f);
                        lightmapRenderState.ambientColor = new Vector3f(vector3fc2.x() * (1.0f - f4) + f * f4, vector3fc2.y() * (1.0f - f4) + f2 * f4, vector3fc2.z() * (1.0f - f4) + f3 * f4);
                    }
                    if (iAmbienceModule.isBlocksEnabled()) {
                        vector3fc = lightmapRenderState.blockLightTint != null ? lightmapRenderState.blockLightTint : new Vector3f(1.0f, 1.0f, 1.0f);
                        lightmapRenderState.blockLightTint = new Vector3f(vector3fc.x() * (1.0f - f4) + f * f4, vector3fc.y() * (1.0f - f4) + f2 * f4, vector3fc.z() * (1.0f - f4) + f3 * f4);
                    }
                    lightmapRenderState.needsUpdate = true;
                }
            }
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }
}

