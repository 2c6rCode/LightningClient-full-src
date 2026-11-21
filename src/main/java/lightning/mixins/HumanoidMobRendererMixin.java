/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lightning.Night
 *  lightning.modules.impl.movement.ElytraFlyModule
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.renderer.entity.HumanoidMobRenderer
 *  net.minecraft.client.renderer.entity.state.HumanoidRenderState
 *  net.minecraft.client.renderer.item.ItemModelResolver
 *  net.minecraft.world.entity.LivingEntity
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package lightning.mixins;

import lightning.Night;
import lightning.modules.impl.movement.ElytraFlyModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={HumanoidMobRenderer.class})
public abstract class HumanoidMobRendererMixin {
    @Inject(method={"extractHumanoidRenderState"}, at={@At(value="TAIL")})
    private static void night$hideControlRocketGlide(LivingEntity livingEntity, HumanoidRenderState humanoidRenderState, float f, ItemModelResolver itemModelResolver, CallbackInfo callbackInfo) {
        try {
            if (livingEntity != Minecraft.getInstance().player) {
                return;
            }
            ElytraFlyModule elytraFlyModule = (ElytraFlyModule)Night.MODULE_MANAGER.getModule(ElytraFlyModule.class);
            if (elytraFlyModule.isToggled() && elytraFlyModule.mode.getValue().equalsIgnoreCase("ControlRocket")) {
                humanoidRenderState.isFallFlying = false;
                humanoidRenderState.speedValue = 1.0f;
            }
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }
}

