/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  lightning.Night
 *  lightning.modules.impl.visuals.ChamsModule
 *  net.minecraft.client.model.Model
 *  net.minecraft.client.model.object.crystal.EndCrystalModel
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package lightning.mixins;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import lightning.Night;
import lightning.modules.impl.visuals.ChamsModule;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.object.crystal.EndCrystalModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={Model.class})
public class CrystalSizeMixin {
    @Inject(method={"renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;III)V"}, at={@At(value="HEAD")}, require=0)
    private void night$crystalSizeIn(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color, CallbackInfo callbackInfo) {
        try {
            float crystalScale;
            ChamsModule chamsModule;
            if ((Object)this instanceof EndCrystalModel && (chamsModule = (ChamsModule)Night.MODULE_MANAGER.getModule(ChamsModule.class)) != null && (crystalScale = chamsModule.crystalSize.getValue().floatValue()) != 1.0f && crystalScale > 0.0f) {
                poseStack.scale(crystalScale, crystalScale, crystalScale);
            }
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }

    @Inject(method={"renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;III)V"}, at={@At(value="RETURN")}, require=0)
    private void night$crystalSizeOut(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color, CallbackInfo callbackInfo) {
        try {
            float crystalScale;
            ChamsModule chamsModule;
            if ((Object)this instanceof EndCrystalModel && (chamsModule = (ChamsModule)Night.MODULE_MANAGER.getModule(ChamsModule.class)) != null && (crystalScale = chamsModule.crystalSize.getValue().floatValue()) != 1.0f && crystalScale > 0.0f) {
                crystalScale = 1.0f / crystalScale;
                poseStack.scale(crystalScale, crystalScale, crystalScale);
            }
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }
}

