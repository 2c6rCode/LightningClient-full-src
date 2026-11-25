/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  lightning.Night
 *  lightning.modules.impl.visuals.ChamsModule
 *  lightning.modules.impl.visuals.ShadersModule
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.model.geom.ModelPart
 *  net.minecraft.client.renderer.SubmitNodeCollector
 *  net.minecraft.client.renderer.entity.player.AvatarRenderer
 *  net.minecraft.client.renderer.entity.state.AvatarRenderState
 *  net.minecraft.client.renderer.rendertype.RenderType
 *  net.minecraft.client.renderer.texture.TextureAtlasSprite
 *  net.minecraft.world.entity.Avatar
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.phys.Vec3
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.Redirect
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package lightning.mixins;

import com.mojang.blaze3d.vertex.PoseStack;
import lightning.Night;
import lightning.modules.impl.visuals.ChamsModule;
import lightning.modules.impl.visuals.ShadersModule;
import lightning.utils.mixins.IChamsCapture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={AvatarRenderer.class})
public class AvatarRendererMixin {
    @Inject(method={"extractRenderState(Lnet/minecraft/world/entity/Avatar;Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;F)V"}, at={@At(value="TAIL")})
    private void night$forceChamsOverlayLayers(Avatar avatar, AvatarRenderState avatarRenderState, float tickDelta, CallbackInfo callbackInfo) {
        try {
            if (!(avatar instanceof Entity)) {
                return;
            }
            Avatar avatar2 = avatar;
            ChamsModule chamsModule = (ChamsModule)Night.MODULE_MANAGER.getModule(ChamsModule.class);
            if (!(chamsModule != null && chamsModule.isToggled() && chamsModule.players.getValue() && chamsModule.isValidEntity((Entity)avatar2))) {
                return;
            }
            avatarRenderState.showHat = true;
            avatarRenderState.showJacket = true;
            avatarRenderState.showLeftSleeve = true;
            avatarRenderState.showRightSleeve = true;
            avatarRenderState.showLeftPants = true;
            avatarRenderState.showRightPants = true;
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }

    @Redirect(method={"renderHand"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/renderer/SubmitNodeCollector;submitModelPart(Lnet/minecraft/client/model/geom/ModelPart;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/rendertype/RenderType;IILnet/minecraft/client/renderer/texture/TextureAtlasSprite;)V")}, require=0)
    private void night$handOutline(SubmitNodeCollector submitNodeCollector, ModelPart modelPart, PoseStack poseStack, RenderType renderType, int packedLight, int packedOverlay, TextureAtlasSprite textureAtlasSprite) {
        submitNodeCollector.submitModelPart(modelPart, poseStack, renderType, packedLight, packedOverlay, textureAtlasSprite, -1, null, AvatarRendererMixin.night$outlineColor());
    }

    private static int night$outlineColor() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return 0;
        }
        ShadersModule shadersModule = (ShadersModule)Night.MODULE_MANAGER.getModule(ShadersModule.class);
        if (shadersModule.isToggled() && shadersModule.isValidEntity((Entity)minecraft.player)) {
            return shadersModule.getColor((Entity)minecraft.player).getRGB() | 0xFF000000;
        }
        return 0;
    }

    @Inject(method={"getRenderOffset(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;)Lnet/minecraft/world/phys/Vec3;"}, at={@At(value="RETURN")}, cancellable=true)
    private void night$applyPopChamYOffset(AvatarRenderState avatarRenderState, CallbackInfoReturnable<Vec3> callbackInfoReturnable) {
        IChamsCapture iChamsCapture = (IChamsCapture)avatarRenderState;
        float yOffset = iChamsCapture.night$chamsYOffset();
        if (yOffset != 0.0f) {
            Vec3 vec3 = (Vec3)callbackInfoReturnable.getReturnValue();
            callbackInfoReturnable.setReturnValue(vec3 == null ? new Vec3(0.0, (double)yOffset, 0.0) : vec3.add(0.0, (double)yOffset, 0.0));
        }
    }
}

