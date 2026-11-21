/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.wrapoperation.Operation
 *  com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation
 *  lightning.Night
 *  lightning.modules.impl.movement.VelocityModule
 *  lightning.utils.IMinecraft
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.projectile.FishingHook
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 */
package lightning.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import lightning.Night;
import lightning.modules.impl.movement.VelocityModule;
import lightning.utils.IMinecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.FishingHook;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value={FishingHook.class})
public class FishingBobberEntityMixin
implements IMinecraft {
    @WrapOperation(method={"handleEntityEvent"}, at={@At(value="INVOKE", target="Lnet/minecraft/world/entity/projectile/FishingHook;pullEntity(Lnet/minecraft/world/entity/Entity;)V")})
    private void pushOutOfBlocks(FishingHook fishingHook, Entity entity, Operation<Void> operation) {
        if (entity == FishingBobberEntityMixin.mc.player && ((VelocityModule)Night.MODULE_MANAGER.getModule(VelocityModule.class)).isToggled() && ((VelocityModule)Night.MODULE_MANAGER.getModule(VelocityModule.class)).antiFishingRod.getValue()) {
            return;
        }
        operation.call(new Object[]{fishingHook, entity});
    }
}

