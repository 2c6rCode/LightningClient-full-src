/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.wrapoperation.Operation
 *  com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation
 *  lightning.modules.impl.movement.AirStuckModule
 *  net.minecraft.client.player.LocalPlayer
 *  net.minecraft.world.entity.MoverType
 *  net.minecraft.world.phys.Vec3
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 */
package lightning.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import lightning.modules.impl.movement.AirStuckModule;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value={LocalPlayer.class}, priority=1000)
public abstract class AirStuckMovementMixin {
    @WrapOperation(method={"move(Lnet/minecraft/world/entity/MoverType;Lnet/minecraft/world/phys/Vec3;)V"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/player/AbstractClientPlayer;move(Lnet/minecraft/world/entity/MoverType;Lnet/minecraft/world/phys/Vec3;)V")}, require=1, expect=1, allow=1)
    private void night$freezeVanillaMovement(LocalPlayer localPlayer, MoverType moverType, Vec3 vec3, Operation<Void> operation) {
        operation.call(new Object[]{localPlayer, moverType, AirStuckModule.movementDisplacement((Vec3)vec3)});
    }
}

