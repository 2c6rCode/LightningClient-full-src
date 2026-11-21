/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.ModifyReturnValue
 *  lightning.Night
 *  lightning.events.Event
 *  lightning.events.impl.PlayerTravelEvent
 *  lightning.modules.api.IPatchModule
 *  lightning.modules.api.IReachModule
 *  lightning.modules.api.ISpeedModule
 *  lightning.modules.impl.combat.AutoMaceModule
 *  lightning.modules.impl.movement.ElytraFlyModule
 *  lightning.modules.impl.movement.VelocityModule
 *  lightning.utils.IMinecraft
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.MoverType
 *  net.minecraft.world.entity.Pose
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.Vec3
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.Redirect
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package lightning.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import lightning.Night;
import lightning.events.Event;
import lightning.events.impl.PlayerTravelEvent;
import lightning.modules.api.IPatchModule;
import lightning.modules.api.IReachModule;
import lightning.modules.api.ISpeedModule;
import lightning.modules.impl.combat.AutoMaceModule;
import lightning.modules.impl.movement.ElytraFlyModule;
import lightning.modules.impl.movement.VelocityModule;
import lightning.utils.IMinecraft;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={Player.class})
public abstract class PlayerEntityMixin
extends LivingEntity
implements IMinecraft {
    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }

    @ModifyReturnValue(method={"isPushedByFluid"}, at={@At(value="RETURN")})
    private boolean isPushedByFluids(boolean bl) {
        try {
            if ((Object)this == PlayerEntityMixin.mc.player && ((VelocityModule)Night.MODULE_MANAGER.getModule(VelocityModule.class)).isToggled() && ((VelocityModule)Night.MODULE_MANAGER.getModule(VelocityModule.class)).antiLiquidPush.getValue()) {
                return false;
            }
            return bl;
        }
        catch (NullPointerException nullPointerException) {
            return bl;
        }
    }

    @Redirect(method={"causeExtraKnockback"}, at={@At(value="INVOKE", target="Lnet/minecraft/world/entity/player/Player;setDeltaMovement(Lnet/minecraft/world/phys/Vec3;)V")})
    private void grimAttackVelocity$setDeltaMovement(Player player, Vec3 vec3) {
        if ((Object)this == PlayerEntityMixin.mc.player && ((IPatchModule)Night.MODULE_MANAGER.getModule("Patch")).isGrimAttackVelocity()) {
            return;
        }
        player.setDeltaMovement(vec3);
    }

    @Redirect(method={"causeExtraKnockback"}, at={@At(value="INVOKE", target="Lnet/minecraft/world/entity/player/Player;setSprinting(Z)V")})
    private void grimAttackVelocity$setSprinting(Player player, boolean bl) {
        if ((Object)this == PlayerEntityMixin.mc.player && ((IPatchModule)Night.MODULE_MANAGER.getModule("Patch")).isGrimAttackVelocity()) {
            return;
        }
        player.setSprinting(bl);
    }

    @Inject(method={"travel"}, at={@At(value="HEAD")}, cancellable=true)
    private void travel(Vec3 vec3, CallbackInfo callbackInfo) {
        try {
            PlayerTravelEvent playerTravelEvent = new PlayerTravelEvent(vec3);
            Night.EVENT_HANDLER.post((Event)playerTravelEvent);
            if (playerTravelEvent.isCancelled()) {
                this.move(MoverType.SELF, this.getDeltaMovement());
                callbackInfo.cancel();
            }
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }

    @Inject(method={"blockInteractionRange"}, at={@At(value="HEAD")}, cancellable=true)
    private void getBlockInteractionRange(CallbackInfoReturnable<Double> callbackInfoReturnable) {
        try {
            IReachModule iReachModule = (IReachModule)Night.MODULE_MANAGER.getModule("Reach");
            if (iReachModule.isToggled()) {
                callbackInfoReturnable.setReturnValue(iReachModule.getAmountValue());
            }
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }

    @Inject(method={"entityInteractionRange"}, at={@At(value="HEAD")}, cancellable=true)
    private void getEntityInteractionRange(CallbackInfoReturnable<Double> callbackInfoReturnable) {
        try {
            IReachModule iReachModule = (IReachModule)Night.MODULE_MANAGER.getModule("Reach");
            if (iReachModule.isToggled()) {
                callbackInfoReturnable.setReturnValue(iReachModule.getAmountValue());
            }
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }

    @ModifyReturnValue(method={"getDesiredPose"}, at={@At(value="RETURN")})
    private Pose night$controlRocketStandingHitbox(Pose pose) {
        try {
            if (pose != Pose.FALL_FLYING || (Object)this != PlayerEntityMixin.mc.player || Night.MODULE_MANAGER == null) {
                return pose;
            }
            ElytraFlyModule elytraFlyModule = (ElytraFlyModule)Night.MODULE_MANAGER.getModule(ElytraFlyModule.class);
            if (elytraFlyModule != null && elytraFlyModule.shouldPinFallFlying()) {
                return Pose.STANDING;
            }
            AutoMaceModule autoMaceModule = (AutoMaceModule)Night.MODULE_MANAGER.getModule(AutoMaceModule.class);
            return autoMaceModule != null && autoMaceModule.shouldPinFallFlying() ? Pose.STANDING : pose;
        }
        catch (NullPointerException nullPointerException) {
            return pose;
        }
    }

    @Inject(method={"getSpeed"}, at={@At(value="HEAD")}, cancellable=true)
    private void getMovementSpeed(CallbackInfoReturnable<Float> callbackInfoReturnable) {
        try {
            ISpeedModule iSpeedModule = (ISpeedModule)Night.MODULE_MANAGER.getModule("Speed");
            if (iSpeedModule.isToggled() && iSpeedModule.getModeValue().equalsIgnoreCase("Vanilla")) {
                callbackInfoReturnable.setReturnValue(Float.valueOf(iSpeedModule.getVanillaSpeedValue()));
            }
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }
}

