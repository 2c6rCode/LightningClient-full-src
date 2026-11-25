/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  lightning.utils.graphics.ChamsVertexConsumer
 *  net.minecraft.client.model.Model
 *  net.minecraft.client.renderer.feature.ModelFeatureRenderer
 *  net.minecraft.client.renderer.feature.ModelFeatureRenderer$Submit
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Redirect
 */
package lightning.mixins;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import lightning.utils.graphics.ChamsVertexConsumer;
import lightning.utils.mixins.IChamsCapture;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value={ModelFeatureRenderer.class})
public class ModelFeatureRendererMixin {
    @Redirect(method={"prepareModel"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/model/Model;renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;III)V")})
    private <S> void night$captureChams(Model<S> model, PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color, ModelFeatureRenderer.Submit<S> submit) {
        IChamsCapture iChamsCapture;
        Object state = submit.state();
        if (state instanceof IChamsCapture && ((iChamsCapture = (IChamsCapture)state).night$chamsFill() || iChamsCapture.night$chamsOutline() || iChamsCapture.night$chamsGlint() || iChamsCapture.night$chamsRealAlpha() < 1.0f)) {
            int tintedColor = color;
            float realAlpha = iChamsCapture.night$chamsRealAlpha();
            if (realAlpha < 1.0f) {
                int alpha = color >>> 24 & 0xFF;
                int scaledAlpha = Math.round((float)alpha * realAlpha);
                tintedColor = color & 0xFFFFFF | scaledAlpha << 24;
            }
            model.renderToBuffer(poseStack, (VertexConsumer)new ChamsVertexConsumer(vertexConsumer, iChamsCapture.night$chamsFill(), iChamsCapture.night$chamsFillColor(), iChamsCapture.night$chamsOutline(), iChamsCapture.night$chamsOutlineColor(), iChamsCapture.night$chamsShine(), iChamsCapture.night$chamsGlint(), iChamsCapture.night$chamsGlintColor(), iChamsCapture.night$chamsGlintParams(), iChamsCapture.night$chamsSuppressReal(), realAlpha), packedLight, packedOverlay, tintedColor);
        } else {
            model.renderToBuffer(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        }
    }
}

