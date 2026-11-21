/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lightning.Night
 *  lightning.events.Event
 *  lightning.events.impl.RemoveFireworkEvent
 *  lightning.utils.IMinecraft
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.entity.projectile.FireworkRocketEntity
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.At$Shift
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package lightning.mixins;

import lightning.Night;
import lightning.events.Event;
import lightning.events.impl.RemoveFireworkEvent;
import lightning.utils.IMinecraft;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={FireworkRocketEntity.class})
public class FireworkRocketEntityMixin
implements IMinecraft {
    @Shadow
    private int life;

    @Inject(method={"tick"}, at={@At(value="INVOKE", target="Lnet/minecraft/world/entity/projectile/FireworkRocketEntity;updateRotation()V", shift=At.Shift.AFTER)}, cancellable=true)
    private void tick(CallbackInfo callbackInfo) {
        try {
            FireworkRocketEntity fireworkRocketEntity = (FireworkRocketEntity)(Object)this;
            RemoveFireworkEvent removeFireworkEvent = new RemoveFireworkEvent(fireworkRocketEntity);
            Night.EVENT_HANDLER.post((Event)removeFireworkEvent);
            if (removeFireworkEvent.isCancelled()) {
                callbackInfo.cancel();
                if (this.life == 0 && !fireworkRocketEntity.isSilent()) {
                    FireworkRocketEntityMixin.mc.level.playSound(null, fireworkRocketEntity.getX(), fireworkRocketEntity.getY(), fireworkRocketEntity.getZ(), SoundEvents.FIREWORK_ROCKET_LAUNCH, SoundSource.AMBIENT, 3.0f, 1.0f);
                }
                ++this.life;
                if (this.life % 2 < 2) {
                    FireworkRocketEntityMixin.mc.level.addParticle((ParticleOptions)ParticleTypes.FIREWORK, fireworkRocketEntity.getX(), fireworkRocketEntity.getY(), fireworkRocketEntity.getZ(), FireworkRocketEntityMixin.mc.level.getRandom().nextGaussian() * 0.05, -fireworkRocketEntity.getDeltaMovement().y * 0.5, FireworkRocketEntityMixin.mc.level.getRandom().nextGaussian() * 0.05);
                }
            }
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }
}

