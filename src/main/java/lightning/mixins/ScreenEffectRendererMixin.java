/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  lightning.Night
 *  lightning.modules.impl.visuals.NoRenderModule
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.renderer.ScreenEffectRenderer
 *  net.minecraft.client.renderer.SubmitNodeCollector
 *  net.minecraft.client.renderer.texture.TextureAtlasSprite
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package lightning.mixins;

import com.mojang.blaze3d.vertex.PoseStack;
import lightning.Night;
import lightning.modules.impl.visuals.NoRenderModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={ScreenEffectRenderer.class})
public class ScreenEffectRendererMixin {
    @Inject(method={"submitFire"}, at={@At(value="HEAD")}, cancellable=true)
    private static void night$renderFire(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, TextureAtlasSprite textureAtlasSprite, CallbackInfo callbackInfo) {
        try {
            NoRenderModule noRenderModule = (NoRenderModule)Night.MODULE_MANAGER.getModule(NoRenderModule.class);
            if (noRenderModule.isToggled() && noRenderModule.fireOverlay.getValue()) {
                callbackInfo.cancel();
            }
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }

    @Inject(method={"submitWater"}, at={@At(value="HEAD")}, cancellable=true)
    private static void night$renderWater(Minecraft minecraft, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CallbackInfo callbackInfo) {
        try {
            NoRenderModule noRenderModule = (NoRenderModule)Night.MODULE_MANAGER.getModule(NoRenderModule.class);
            if (noRenderModule.isToggled() && noRenderModule.liquidOverlay.getValue()) {
                callbackInfo.cancel();
            }
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }

    @Inject(method={"submitBlockSprite"}, at={@At(value="HEAD")}, cancellable=true)
    private static void night$renderTex(TextureAtlasSprite textureAtlasSprite, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int n, CallbackInfo callbackInfo) {
        try {
            NoRenderModule noRenderModule = (NoRenderModule)Night.MODULE_MANAGER.getModule(NoRenderModule.class);
            if (noRenderModule.isToggled() && noRenderModule.blockOverlay.getValue()) {
                callbackInfo.cancel();
            }
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }
}

