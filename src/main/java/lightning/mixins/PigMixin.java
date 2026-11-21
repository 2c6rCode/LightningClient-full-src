/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lightning.Night
 *  lightning.modules.impl.movement.EntityControlModule
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.animal.pig.Pig
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package lightning.mixins;

import lightning.Night;
import lightning.modules.impl.movement.EntityControlModule;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.pig.Pig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={Pig.class})
public abstract class PigMixin {
    @Inject(method={"getControllingPassenger"}, at={@At(value="HEAD")}, cancellable=true)
    private void night$onGetControllingPassenger(CallbackInfoReturnable<LivingEntity> callbackInfoReturnable) {
        try {
            Entity entity;
            EntityControlModule entityControlModule;
            if (Night.MODULE_MANAGER != null && (entityControlModule = (EntityControlModule)Night.MODULE_MANAGER.getModule(EntityControlModule.class)) != null && entityControlModule.isToggled() && entityControlModule.control.getValue() && (entity = ((Pig)(Object)this).getFirstPassenger()) instanceof LivingEntity) {
                LivingEntity livingEntity = (LivingEntity)entity;
                callbackInfoReturnable.setReturnValue(livingEntity);
            }
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }
}

