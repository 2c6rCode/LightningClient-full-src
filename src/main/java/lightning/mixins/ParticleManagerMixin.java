/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lightning.Night
 *  lightning.modules.impl.visuals.NoRenderModule
 *  lightning.modules.impl.visuals.ParticlesModule
 *  lightning.utils.IMinecraft
 *  net.minecraft.client.particle.Particle
 *  net.minecraft.client.particle.ParticleEngine
 *  net.minecraft.client.particle.TotemParticle
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.world.entity.Entity
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package lightning.mixins;

import java.awt.Color;
import lightning.Night;
import lightning.modules.impl.visuals.NoRenderModule;
import lightning.modules.impl.visuals.ParticlesModule;
import lightning.utils.IMinecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.TotemParticle;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={ParticleEngine.class})
public class ParticleManagerMixin
implements IMinecraft {
    @Unique
    private static int night$totemCounter = 0;
    @Unique
    private static long night$lastTotemTime = 0L;

    @Inject(method={"createTrackingEmitter(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/core/particles/ParticleOptions;I)V"}, at={@At(value="HEAD")}, cancellable=true)
    private void night$onCreateTrackingEmitter(Entity entity, ParticleOptions particleOptions, int n, CallbackInfo callbackInfo) {
        try {
            NoRenderModule noRenderModule = Night.MODULE_MANAGER != null ? (NoRenderModule)Night.MODULE_MANAGER.getModule(NoRenderModule.class) : null;
            NoRenderModule noRenderModule2 = noRenderModule;
            if (noRenderModule != null && noRenderModule.isToggled() && noRenderModule.totemPop.getValue() && particleOptions.getType() == ParticleTypes.TOTEM_OF_UNDYING && entity == ParticleManagerMixin.mc.player) {
                callbackInfo.cancel();
            }
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }

    @Inject(method={"createParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)Lnet/minecraft/client/particle/Particle;"}, at={@At(value="HEAD")}, cancellable=true)
    private void addParticle(ParticleOptions particleOptions, double d, double d2, double d3, double d4, double d5, double d6, CallbackInfoReturnable<Particle> callbackInfoReturnable) {
        try {
            NoRenderModule noRenderModule = Night.MODULE_MANAGER != null ? (NoRenderModule)Night.MODULE_MANAGER.getModule(NoRenderModule.class) : null;
            NoRenderModule noRenderModule2 = noRenderModule;
            if (noRenderModule != null && noRenderModule.isToggled() && noRenderModule.explosions.getValue() && particleOptions.getType() == ParticleTypes.EXPLOSION) {
                callbackInfoReturnable.cancel();
                return;
            }
            ParticlesModule particlesModule = Night.MODULE_MANAGER != null ? (ParticlesModule)Night.MODULE_MANAGER.getModule(ParticlesModule.class) : null;
            ParticlesModule particlesModule2 = particlesModule;
            if (particlesModule != null && particlesModule.isToggled() && particlesModule.totem.getValue() && particleOptions.getType() == ParticleTypes.TOTEM_OF_UNDYING) {
                if (particlesModule.totemMode.getValue().equalsIgnoreCase("Custom")) {
                    callbackInfoReturnable.cancel();
                } else if (particlesModule.totemMode.getValue().equalsIgnoreCase("Vanilla")) {
                    long l = System.currentTimeMillis();
                    if (l - night$lastTotemTime > 400L) {
                        night$totemCounter = 0;
                        night$lastTotemTime = l;
                    }
                    if (night$totemCounter >= particlesModule.totemNumber.getValue().intValue()) {
                        callbackInfoReturnable.cancel();
                        return;
                    }
                    ++night$totemCounter;
                }
            }
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }

    @Inject(method={"createParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)Lnet/minecraft/client/particle/Particle;"}, at={@At(value="RETURN")})
    private void onCreatedParticle(ParticleOptions particleOptions, double d, double d2, double d3, double d4, double d5, double d6, CallbackInfoReturnable<Particle> callbackInfoReturnable) {
        try {
            if (particleOptions.getType() == ParticleTypes.TOTEM_OF_UNDYING) {
                Particle particle;
                ParticlesModule particlesModule = Night.MODULE_MANAGER != null ? (ParticlesModule)Night.MODULE_MANAGER.getModule(ParticlesModule.class) : null;
                ParticlesModule particlesModule2 = particlesModule;
                if (particlesModule != null && particlesModule.isToggled() && particlesModule.totem.getValue() && particlesModule.totemMode.getValue().equalsIgnoreCase("Vanilla") && (particle = (Particle)callbackInfoReturnable.getReturnValue()) instanceof TotemParticle) {
                    TotemParticle totemParticle = (TotemParticle)particle;
                    Color color = particlesModule.vanillaColorA.getColor();
                    Color color2 = particlesModule.vanillaColorB.getColor();
                    totemParticle.setColor(color.getRGB());
                    totemParticle.setFadeColor(color2.getRGB());
                }
            }
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }
}

