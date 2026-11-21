/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lightning.Night
 *  lightning.modules.impl.movement.EntityControlModule
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.animal.equine.AbstractHorse
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
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={AbstractHorse.class})
public abstract class AbstractHorseMixin {
    @Inject(method={"getControllingPassenger"}, at={@At(value="HEAD")}, cancellable=true)
    private void night$onGetControllingPassenger(CallbackInfoReturnable<LivingEntity> callbackInfoReturnable) {
        try {
            Entity entity;
            EntityControlModule entityControlModule;
            if (Night.MODULE_MANAGER != null && (entityControlModule = (EntityControlModule)Night.MODULE_MANAGER.getModule(EntityControlModule.class)) != null && entityControlModule.isToggled() && entityControlModule.control.getValue() && (entity = ((AbstractHorse)(Object)this).getFirstPassenger()) instanceof LivingEntity) {
                LivingEntity livingEntity = (LivingEntity)entity;
                callbackInfoReturnable.setReturnValue(livingEntity);
            }
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }

    @Inject(method={"isTamed"}, at={@At(value="HEAD")}, cancellable=true)
    private void night$onIsTamed(CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        try {
            EntityControlModule entityControlModule;
            if (Night.MODULE_MANAGER != null && (entityControlModule = (EntityControlModule)Night.MODULE_MANAGER.getModule(EntityControlModule.class)) != null && entityControlModule.isToggled() && entityControlModule.control.getValue()) {
                callbackInfoReturnable.setReturnValue(true);
            }
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }
}

