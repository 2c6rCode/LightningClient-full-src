/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.pipeline.RenderTarget
 *  com.mojang.blaze3d.pipeline.TextureTarget
 *  com.mojang.blaze3d.resource.GraphicsResourceAllocator
 *  com.mojang.blaze3d.systems.CommandEncoder
 *  com.mojang.blaze3d.systems.RenderPass
 *  com.mojang.blaze3d.textures.GpuTextureView
 *  com.mojang.blaze3d.vertex.PoseStack
 *  lightning.Night
 *  lightning.modules.impl.visuals.AtmosphereModule
 *  lightning.utils.graphics.EspShader
 *  lightning.utils.graphics.SkyShader
 *  lightning.utils.graphics.StarCapture
 *  net.minecraft.client.Camera
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.multiplayer.ClientLevel
 *  net.minecraft.client.renderer.LevelTargetBundle
 *  net.minecraft.client.renderer.PostChain
 *  net.minecraft.client.renderer.SkyRenderer
 *  net.minecraft.client.renderer.state.level.SkyRenderState
 *  net.minecraft.resources.Identifier
 *  net.minecraft.world.level.MoonPhase
 *  net.minecraft.world.level.dimension.DimensionType$Skybox
 *  org.joml.Vector4f
 *  org.joml.Vector4fc
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.ModifyVariable
 *  org.spongepowered.asm.mixin.injection.Redirect
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package lightning.mixins;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.function.Supplier;
import lightning.Night;
import lightning.modules.impl.visuals.AtmosphereModule;
import lightning.utils.graphics.EspShader;
import lightning.utils.graphics.SkyShader;
import lightning.utils.graphics.StarCapture;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelTargetBundle;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.SkyRenderer;
import net.minecraft.client.renderer.state.level.SkyRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.MoonPhase;
import net.minecraft.world.level.dimension.DimensionType;
import org.joml.Vector4f;
import org.joml.Vector4fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={SkyRenderer.class})
public abstract class SkyRendererMixin {
    @Redirect(method={"renderStars"}, at={@At(value="INVOKE", target="Lcom/mojang/blaze3d/systems/CommandEncoder;createRenderPass(Ljava/util/function/Supplier;Lcom/mojang/blaze3d/textures/GpuTextureView;Ljava/util/Optional;Lcom/mojang/blaze3d/textures/GpuTextureView;Ljava/util/OptionalDouble;)Lcom/mojang/blaze3d/systems/RenderPass;")})
    private RenderPass night$redirectStars(CommandEncoder commandEncoder, Supplier<String> supplier, GpuTextureView gpuTextureView, Optional<Vector4fc> optional, GpuTextureView gpuTextureView2, OptionalDouble optionalDouble) {
        AtmosphereModule atmosphereModule = (AtmosphereModule)Night.MODULE_MANAGER.getModule(AtmosphereModule.class);
        if (atmosphereModule.isToggled() && atmosphereModule.starGlow.getValue()) {
            TextureTarget textureTarget = StarCapture.ensure();
            return commandEncoder.createRenderPass(supplier, textureTarget.getColorTextureView(), Optional.of(new Vector4f(0.0f, 0.0f, 0.0f, 0.0f)), gpuTextureView2, optionalDouble);
        }
        return commandEncoder.createRenderPass(supplier, gpuTextureView, optional, gpuTextureView2, optionalDouble);
    }

    @Inject(method={"renderSunMoonAndStars"}, at={@At(value="TAIL")})
    private void night$resolveStarGlow(PoseStack poseStack, float f, float f2, float f3, MoonPhase moonPhase, float f4, float f5, CallbackInfo callbackInfo) {
        try {
            if (f5 <= 0.0f) {
                return;
            }
            AtmosphereModule atmosphereModule = (AtmosphereModule)Night.MODULE_MANAGER.getModule(AtmosphereModule.class);
            if (!atmosphereModule.isToggled() || !atmosphereModule.starGlow.getValue()) {
                return;
            }
            TextureTarget textureTarget = StarCapture.get();
            if (textureTarget == null) {
                return;
            }
            Minecraft minecraft = Minecraft.getInstance();
            PostChain postChain = minecraft.getShaderManager().getPostChain(Identifier.fromNamespaceAndPath((String)"lightning", (String)"star_glow"), LevelTargetBundle.MAIN_TARGETS);
            if (postChain == null) {
                return;
            }
            EspShader.writeStarGlowSettings((PostChain)postChain, (float)atmosphereModule.starGlowIntensity.getValue().floatValue());
            postChain.process((RenderTarget)textureTarget, GraphicsResourceAllocator.UNPOOLED);
            textureTarget.blitAndBlendToTexture(minecraft.gameRenderer.mainRenderTarget().getColorTextureView(), minecraft.gameRenderer.mainRenderTarget().getDepthTextureView());
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }

    @Inject(method={"extractRenderState"}, at={@At(value="RETURN")})
    private void night$forceSkybox(ClientLevel clientLevel, float f, Camera camera, SkyRenderState skyRenderState, CallbackInfo callbackInfo) {
        try {
            AtmosphereModule atmosphereModule = Night.MODULE_MANAGER != null ? (AtmosphereModule)Night.MODULE_MANAGER.getModule(AtmosphereModule.class) : null;
            AtmosphereModule atmosphereModule2 = atmosphereModule;
            if (atmosphereModule != null && atmosphereModule.isToggled() && atmosphereModule.modifySky.getValue()) {
                skyRenderState.skybox = DimensionType.Skybox.OVERWORLD;
            }
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }

    @Inject(method={"renderSkyDisc"}, at={@At(value="HEAD")}, cancellable=true)
    private void night$renderCustomSkyDisc(int n, CallbackInfo callbackInfo) {
        try {
            AtmosphereModule atmosphereModule = Night.MODULE_MANAGER != null ? (AtmosphereModule)Night.MODULE_MANAGER.getModule(AtmosphereModule.class) : null;
            AtmosphereModule atmosphereModule2 = atmosphereModule;
            if (atmosphereModule != null && atmosphereModule.isToggled() && atmosphereModule.modifySky.getValue() && atmosphereModule.skyMode.getValue().equalsIgnoreCase("Custom")) {
                SkyShader.renderSky();
                callbackInfo.cancel();
            }
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }

    @ModifyVariable(method={"renderSkyDisc"}, at={@At(value="HEAD")}, argsOnly=true, ordinal=0)
    private int night$modifySkyColor(int n) {
        try {
            AtmosphereModule atmosphereModule = Night.MODULE_MANAGER != null ? (AtmosphereModule)Night.MODULE_MANAGER.getModule(AtmosphereModule.class) : null;
            AtmosphereModule atmosphereModule2 = atmosphereModule;
            if (atmosphereModule != null && atmosphereModule.isToggled() && atmosphereModule.modifySky.getValue() && atmosphereModule.skyMode.getValue().equalsIgnoreCase("Vanilla")) {
                return atmosphereModule.skyColor.getColor().getRGB();
            }
            return n;
        }
        catch (NullPointerException nullPointerException) {
            return n;
        }
    }

    @Inject(method={"renderDarkDisc"}, at={@At(value="HEAD")}, cancellable=true)
    private void night$cancelDarkDisc(CallbackInfo callbackInfo) {
        try {
            AtmosphereModule atmosphereModule = Night.MODULE_MANAGER != null ? (AtmosphereModule)Night.MODULE_MANAGER.getModule(AtmosphereModule.class) : null;
            AtmosphereModule atmosphereModule2 = atmosphereModule;
            if (atmosphereModule != null && atmosphereModule.isToggled() && atmosphereModule.modifySky.getValue()) {
                callbackInfo.cancel();
            }
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }

    @Inject(method={"renderSunriseAndSunset"}, at={@At(value="HEAD")}, cancellable=true)
    private void night$cancelSunriseAndSunset(CallbackInfo callbackInfo) {
        try {
            AtmosphereModule atmosphereModule = Night.MODULE_MANAGER != null ? (AtmosphereModule)Night.MODULE_MANAGER.getModule(AtmosphereModule.class) : null;
            AtmosphereModule atmosphereModule2 = atmosphereModule;
            if (atmosphereModule != null && atmosphereModule.isToggled() && atmosphereModule.modifySky.getValue()) {
                callbackInfo.cancel();
            }
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }

    @Inject(method={"renderSunMoonAndStars"}, at={@At(value="HEAD")}, cancellable=true)
    private void night$cancelSunMoonAndStars(PoseStack poseStack, float f, float f2, float f3, MoonPhase moonPhase, float f4, float f5, CallbackInfo callbackInfo) {
        try {
            AtmosphereModule atmosphereModule = Night.MODULE_MANAGER != null ? (AtmosphereModule)Night.MODULE_MANAGER.getModule(AtmosphereModule.class) : null;
            AtmosphereModule atmosphereModule2 = atmosphereModule;
            if (atmosphereModule != null && atmosphereModule.isToggled() && atmosphereModule.modifySky.getValue()) {
                Minecraft minecraft = Minecraft.getInstance();
                boolean bl = minecraft.level != null && minecraft.level.dimensionType().skybox() == DimensionType.Skybox.NONE;
                boolean bl2 = bl;
                if (bl || atmosphereModule.skyMode.getValue().equalsIgnoreCase("Custom") && !atmosphereModule.starGlow.getValue()) {
                    callbackInfo.cancel();
                }
            }
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }

    @Inject(method={"renderEndSky"}, at={@At(value="HEAD")}, cancellable=true)
    private void night$renderCustomEndSky(CallbackInfo callbackInfo) {
        try {
            AtmosphereModule atmosphereModule = Night.MODULE_MANAGER != null ? (AtmosphereModule)Night.MODULE_MANAGER.getModule(AtmosphereModule.class) : null;
            AtmosphereModule atmosphereModule2 = atmosphereModule;
            if (atmosphereModule != null && atmosphereModule.isToggled() && atmosphereModule.modifySky.getValue() && atmosphereModule.skyMode.getValue().equalsIgnoreCase("Custom")) {
                SkyShader.renderSky();
                callbackInfo.cancel();
            }
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }
}

