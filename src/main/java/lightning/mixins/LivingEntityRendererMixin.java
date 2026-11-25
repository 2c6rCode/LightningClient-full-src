/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  lightning.Night
 *  lightning.modules.impl.movement.ElytraFlyModule
 *  lightning.modules.impl.visuals.NoRenderModule
 *  lightning.utils.IMinecraft
 *  net.minecraft.client.model.EntityModel
 *  net.minecraft.client.renderer.SubmitNodeCollector
 *  net.minecraft.client.renderer.entity.EntityRenderer
 *  net.minecraft.client.renderer.entity.EntityRendererProvider$Context
 *  net.minecraft.client.renderer.entity.LivingEntityRenderer
 *  net.minecraft.client.renderer.entity.state.AvatarRenderState
 *  net.minecraft.client.renderer.entity.state.LivingEntityRenderState
 *  net.minecraft.client.renderer.state.level.CameraRenderState
 *  net.minecraft.util.Mth
 *  net.minecraft.world.entity.LivingEntity
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package lightning.mixins;

import com.mojang.blaze3d.vertex.PoseStack;
import lightning.Night;
import lightning.modules.impl.movement.ElytraFlyModule;
import lightning.modules.impl.visuals.NoRenderModule;
import lightning.utils.IMinecraft;
import lightning.utils.mixins.ISelfState;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={LivingEntityRenderer.class})
public abstract class LivingEntityRendererMixin<T extends LivingEntity, S extends LivingEntityRenderState, M extends EntityModel<? super S>>
extends EntityRenderer<T, S>
implements IMinecraft {
    private static final double SAME_SPOT_RANGE_SQ = 1.0;

    public LivingEntityRendererMixin(EntityRendererProvider.Context context) {
        super(context);
    }

    @Inject(method={"submit"}, at={@At(value="HEAD")}, cancellable=true)
    private void submit(S renderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState, CallbackInfo callbackInfo) {
        try {
            NoRenderModule noRenderModule = (NoRenderModule)Night.MODULE_MANAGER.getModule(NoRenderModule.class);
            if (!noRenderModule.isToggled()) {
                return;
            }
            if (noRenderModule.corpses.getValue() && ((LivingEntityRenderState)renderState).deathTime > 0.0f) {
                callbackInfo.cancel();
                return;
            }
            if (noRenderModule.player.getValue() && renderState instanceof AvatarRenderState && !((ISelfState)renderState).night$isSelf() && LivingEntityRendererMixin.mc.player != null) {
                double dx = ((LivingEntityRenderState)renderState).x - LivingEntityRendererMixin.mc.player.getX();
                double dz = ((LivingEntityRenderState)renderState).z - LivingEntityRendererMixin.mc.player.getZ();
                if (dx * dx + dz * dz <= 1.0) {
                    callbackInfo.cancel();
                }
            }
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }

    @Inject(method={"extractRenderState"}, at={@At(value="TAIL")})
    private void night$extractRenderState(T entity, S renderState, float partialTick, CallbackInfo callbackInfo) {
        try {
            ElytraFlyModule elytraFlyModule;
            ((ISelfState)renderState).night$setSelf(entity == LivingEntityRendererMixin.mc.player);
            if (entity == LivingEntityRendererMixin.mc.player && Night.ROTATION_MANAGER.inRenderTime()) {
                ((LivingEntityRenderState)renderState).bodyRot = Night.ROTATION_MANAGER.getRenderRotations()[0];
                ((LivingEntityRenderState)renderState).yRot = Mth.wrapDegrees((float)(Night.ROTATION_MANAGER.getRenderRotations()[0] - ((LivingEntityRenderState)renderState).bodyRot));
                ((LivingEntityRenderState)renderState).xRot = Night.ROTATION_MANAGER.getRenderRotations()[1];
                return;
            }
            if (entity == LivingEntityRendererMixin.mc.player && Night.MODULE_MANAGER != null && (elytraFlyModule = (ElytraFlyModule)Night.MODULE_MANAGER.getModule(ElytraFlyModule.class)) != null && elytraFlyModule.isToggled() && elytraFlyModule.mode.getValue().equalsIgnoreCase("Bounce") && elytraFlyModule.bounceAutoPitch.getValue()) {
                ((LivingEntityRenderState)renderState).xRot = elytraFlyModule.bouncePitch.getValue().floatValue();
            }
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }
}

