/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.sugar.Local
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.math.Axis
 *  lightning.Night
 *  lightning.modules.impl.player.SwingModule
 *  lightning.modules.impl.visuals.ShadersModule
 *  lightning.modules.impl.visuals.ViewModelModule
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.player.AbstractClientPlayer
 *  net.minecraft.client.player.LocalPlayer
 *  net.minecraft.client.renderer.ItemInHandRenderer
 *  net.minecraft.client.renderer.SubmitNodeCollector
 *  net.minecraft.util.Mth
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.HumanoidArm
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  org.joml.Quaternionfc
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.ModifyArg
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package lightning.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import lightning.Night;
import lightning.modules.impl.player.SwingModule;
import lightning.modules.impl.visuals.ShadersModule;
import lightning.modules.impl.visuals.ViewModelModule;
import lightning.utils.mixins.HandsRenderState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.joml.Quaternionfc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={ItemInHandRenderer.class})
public abstract class ItemInHandRendererMixin {
    @Shadow
    private float mainHandHeight;
    @Shadow
    private float oMainHandHeight;
    @Shadow
    private float offHandHeight;
    @Shadow
    private float oOffHandHeight;
    @Shadow
    private ItemStack mainHandItem;
    @Shadow
    private ItemStack offHandItem;

    @Inject(method={"submitHandsWithItems"}, at={@At(value="HEAD")})
    private void night$startHands(float f, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, LocalPlayer localPlayer, int n, CallbackInfo callbackInfo) {
        HandsRenderState.renderingHands = true;
    }

    @Inject(method={"tick"}, at={@At(value="TAIL")})
    private void night$tickViewModel(CallbackInfo callbackInfo) {
        try {
            ViewModelModule viewModelModule = Night.MODULE_MANAGER != null ? (ViewModelModule)Night.MODULE_MANAGER.getModule(ViewModelModule.class) : null;
            ViewModelModule viewModelModule2 = viewModelModule;
            if (viewModelModule == null || !viewModelModule.isToggled()) {
                return;
            }
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.player == null) {
                return;
            }
            if (!viewModelModule.mainhandSwap.getValue()) {
                this.mainHandHeight = 1.0f;
                this.oMainHandHeight = 1.0f;
                this.mainHandItem = minecraft.player.getMainHandItem();
            }
            if (!viewModelModule.offhandSwap.getValue()) {
                this.offHandHeight = 1.0f;
                this.oOffHandHeight = 1.0f;
                this.offHandItem = minecraft.player.getOffhandItem();
            }
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }

    @Inject(method={"submitArmWithItem"}, at={@At(value="HEAD")})
    private void night$applyViewModelTransformHead(AbstractClientPlayer abstractClientPlayer, float f, float f2, InteractionHand interactionHand, float f3, ItemStack itemStack, float f4, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int n, CallbackInfo callbackInfo) {
        try {
            ViewModelModule viewModelModule = Night.MODULE_MANAGER != null ? (ViewModelModule)Night.MODULE_MANAGER.getModule(ViewModelModule.class) : null;
            ViewModelModule viewModelModule2 = viewModelModule;
            if (viewModelModule == null || !viewModelModule.isToggled()) {
                return;
            }
            poseStack.pushPose();
            boolean bl = interactionHand == InteractionHand.MAIN_HAND || viewModelModule.mode.getValue().equalsIgnoreCase("Both");
            boolean bl2 = bl;
            if (bl) {
                poseStack.translate(viewModelModule.positionMainX.getValue().floatValue(), viewModelModule.positionMainY.getValue().floatValue(), viewModelModule.positionMainZ.getValue().floatValue());
                poseStack.scale(viewModelModule.scaleMainX.getValue().floatValue(), viewModelModule.scaleMainY.getValue().floatValue(), viewModelModule.scaleMainZ.getValue().floatValue());
                poseStack.mulPose((Quaternionfc)Axis.XP.rotationDegrees(viewModelModule.rotationMainX.getValue().floatValue()));
                poseStack.mulPose((Quaternionfc)Axis.YP.rotationDegrees(viewModelModule.rotationMainY.getValue().floatValue()));
                poseStack.mulPose((Quaternionfc)Axis.ZP.rotationDegrees(viewModelModule.rotationMainZ.getValue().floatValue()));
            } else {
                poseStack.translate(viewModelModule.positionOffX.getValue().floatValue(), viewModelModule.positionOffY.getValue().floatValue(), viewModelModule.positionOffZ.getValue().floatValue());
                poseStack.scale(viewModelModule.scaleOffX.getValue().floatValue(), viewModelModule.scaleOffY.getValue().floatValue(), viewModelModule.scaleOffZ.getValue().floatValue());
                poseStack.mulPose((Quaternionfc)Axis.XP.rotationDegrees(viewModelModule.rotationOffX.getValue().floatValue()));
                poseStack.mulPose((Quaternionfc)Axis.YP.rotationDegrees(viewModelModule.rotationOffY.getValue().floatValue()));
                poseStack.mulPose((Quaternionfc)Axis.ZP.rotationDegrees(viewModelModule.rotationOffZ.getValue().floatValue()));
            }
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }

    @Inject(method={"submitArmWithItem"}, at={@At(value="RETURN")})
    private void night$applyViewModelTransformReturn(AbstractClientPlayer abstractClientPlayer, float f, float f2, InteractionHand interactionHand, float f3, ItemStack itemStack, float f4, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int n, CallbackInfo callbackInfo) {
        try {
            ViewModelModule viewModelModule = Night.MODULE_MANAGER != null ? (ViewModelModule)Night.MODULE_MANAGER.getModule(ViewModelModule.class) : null;
            ViewModelModule viewModelModule2 = viewModelModule;
            if (viewModelModule == null || !viewModelModule.isToggled()) {
                return;
            }
            poseStack.popPose();
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }

    @Inject(method={"applyEatTransform"}, at={@At(value="HEAD")}, cancellable=true)
    private void night$applyEatTransform(PoseStack poseStack, float f, HumanoidArm humanoidArm, ItemStack itemStack, Player player, CallbackInfo callbackInfo) {
        try {
            ViewModelModule viewModelModule = Night.MODULE_MANAGER != null ? (ViewModelModule)Night.MODULE_MANAGER.getModule(ViewModelModule.class) : null;
            ViewModelModule viewModelModule2 = viewModelModule;
            if (viewModelModule == null || !viewModelModule.isToggled()) {
                return;
            }
            float f2 = viewModelModule.eatingMultiplier.getValue().floatValue();
            if (f2 < 1.0f) {
                float f3;
                float f4 = (float)player.getUseItemRemainingTicks() - f + 1.0f;
                float f5 = f4 / (float)itemStack.getUseDuration((LivingEntity)player);
                if (f5 < 0.8f) {
                    f3 = Mth.abs((float)(Mth.cos((double)(f4 / 4.0f * (float)Math.PI)) * 0.1f)) * f2;
                    poseStack.translate(0.0f, f3, 0.0f);
                }
                f3 = 1.0f - (float)Math.pow(f5, 27.0);
                int n = humanoidArm == HumanoidArm.RIGHT ? 1 : -1;
                poseStack.translate(f3 * 0.6f * (float)n, f3 * -0.5f, f3 * 0.0f);
                poseStack.mulPose((Quaternionfc)Axis.YP.rotationDegrees((float)n * f3 * 90.0f));
                poseStack.mulPose((Quaternionfc)Axis.XP.rotationDegrees(f3 * 10.0f));
                poseStack.mulPose((Quaternionfc)Axis.ZP.rotationDegrees((float)n * f3 * 30.0f));
                callbackInfo.cancel();
            }
            if (viewModelModule.eatAnimation.getValue()) {
                poseStack.translate(viewModelModule.eatX.getValue().floatValue(), viewModelModule.eatY.getValue().floatValue(), 0.0f);
            }
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }

    @ModifyArg(method={"renderItem(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;I)V"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/renderer/item/ItemStackRenderState;submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;III)V")}, index=4)
    private int night$handsOutline(int n, @Local(argsOnly=true) ItemStack itemStack) {
        return ItemInHandRendererMixin.computeHandsOutlineColor(n);
    }

    private static int computeHandsOutlineColor(int n) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return n;
        }
        ShadersModule shadersModule = (ShadersModule)Night.MODULE_MANAGER.getModule(ShadersModule.class);
        if (shadersModule.isToggled() && shadersModule.hands.getValue()) {
            return shadersModule.getFillColor((Entity)minecraft.player);
        }
        return n;
    }

    @Inject(method={"submitHandsWithItems"}, at={@At(value="RETURN")})
    private void night$flushHandOutline(float f, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, LocalPlayer localPlayer, int n, CallbackInfo callbackInfo) {
        HandsRenderState.renderingHands = false;
    }

    @Inject(method={"swingArm"}, at={@At(value="HEAD")}, cancellable=true)
    private void night$swingArm(float f, PoseStack poseStack, int n, HumanoidArm humanoidArm, CallbackInfo callbackInfo) {
        try {
            SwingModule swingModule = (SwingModule)Night.MODULE_MANAGER.getModule(SwingModule.class);
            if (!swingModule.isToggled()) {
                return;
            }
            float f2 = swingModule.translateX.getValue() ? -0.4f * Mth.sin((double)(Mth.sqrt((float)f) * (float)Math.PI)) : 0.0f;
            float f3 = swingModule.translateY.getValue() ? 0.2f * Mth.sin((double)(Mth.sqrt((float)f) * ((float)Math.PI * 2))) : 0.0f;
            float f4 = swingModule.translateZ.getValue() ? -0.2f * Mth.sin((double)(f * (float)Math.PI)) : 0.0f;
            poseStack.translate((float)n * f2, f3, f4);
            ItemInHandRendererMixin.night$applyItemArmAttackTransform(poseStack, humanoidArm, f, swingModule);
            callbackInfo.cancel();
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }

    private static void night$applyItemArmAttackTransform(PoseStack poseStack, HumanoidArm humanoidArm, float f, SwingModule swingModule) {
        int n = humanoidArm == HumanoidArm.RIGHT ? 1 : -1;
        float f2 = swingModule.rotationY.getValue() ? Mth.sin((double)(f * f * (float)Math.PI)) : 0.0f;
        poseStack.mulPose((Quaternionfc)Axis.YP.rotationDegrees((float)n * (45.0f + f2 * -20.0f)));
        float f3 = Mth.sin((double)(Mth.sqrt((float)f) * (float)Math.PI));
        poseStack.mulPose((Quaternionfc)Axis.ZP.rotationDegrees((float)n * (swingModule.rotationZ.getValue() ? f3 : 0.0f) * -20.0f));
        poseStack.mulPose((Quaternionfc)Axis.XP.rotationDegrees((swingModule.rotationX.getValue() ? f3 : 0.0f) * -80.0f));
        poseStack.mulPose((Quaternionfc)Axis.YP.rotationDegrees((float)n * -45.0f));
    }
}

