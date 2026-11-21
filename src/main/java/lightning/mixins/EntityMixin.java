/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.ModifyExpressionValue
 *  com.llamalad7.mixinextras.injector.ModifyReturnValue
 *  com.llamalad7.mixinextras.injector.wrapoperation.Operation
 *  com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation
 *  lightning.Night
 *  lightning.events.Event
 *  lightning.events.impl.ChangePitchEvent
 *  lightning.events.impl.ChangeYawEvent
 *  lightning.events.impl.UpdateVelocityEvent
 *  lightning.managers.RotationManager
 *  lightning.modules.api.IFreecamModule
 *  lightning.modules.impl.combat.AutoMaceModule
 *  lightning.modules.impl.miscellaneous.FakePlayerModule
 *  lightning.modules.impl.movement.ElytraFlyModule
 *  lightning.modules.impl.movement.NoSlowModule
 *  lightning.modules.impl.movement.VelocityModule
 *  lightning.modules.impl.player.RotationLockModule
 *  lightning.modules.impl.visuals.LogoutSpotModule
 *  lightning.modules.impl.visuals.PopChamsModule
 *  lightning.utils.IMinecraft
 *  lightning.utils.minecraft.EntityUtils
 *  net.minecraft.client.player.RemotePlayer
 *  net.minecraft.tags.TagKey
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntityFluidInteraction
 *  net.minecraft.world.entity.Pose
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.level.material.Fluid
 *  net.minecraft.world.phys.Vec3
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package lightning.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import lightning.Night;
import lightning.events.Event;
import lightning.events.impl.ChangePitchEvent;
import lightning.events.impl.ChangeYawEvent;
import lightning.events.impl.UpdateVelocityEvent;
import lightning.managers.RotationManager;
import lightning.modules.api.IFreecamModule;
import lightning.modules.impl.combat.AutoMaceModule;
import lightning.modules.impl.miscellaneous.FakePlayerModule;
import lightning.modules.impl.movement.ElytraFlyModule;
import lightning.modules.impl.movement.NoSlowModule;
import lightning.modules.impl.movement.VelocityModule;
import lightning.modules.impl.player.RotationLockModule;
import lightning.modules.impl.visuals.LogoutSpotModule;
import lightning.modules.impl.visuals.PopChamsModule;
import lightning.utils.IMinecraft;
import lightning.utils.minecraft.EntityUtils;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityFluidInteraction;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={Entity.class})
public abstract class EntityMixin
implements IMinecraft {
    @Shadow
    private float yRot;
    @Shadow
    private float xRot;

    @Inject(method={"tick"}, at={@At(value="HEAD")}, cancellable=true)
    private void night$cancelGhostTick(CallbackInfo callbackInfo) {
        try {
            if ((Object)this instanceof RemotePlayer remotePlayer) {
                PopChamsModule popChamsModule = Night.MODULE_MANAGER != null ? (PopChamsModule)Night.MODULE_MANAGER.getModule(PopChamsModule.class) : null;
                if (popChamsModule != null && popChamsModule.isGhost((Entity)(Object)remotePlayer)) {
                    callbackInfo.cancel();
                    return;
                }
                LogoutSpotModule logoutSpotModule = Night.MODULE_MANAGER != null ? (LogoutSpotModule)Night.MODULE_MANAGER.getModule(LogoutSpotModule.class) : null;
                if (logoutSpotModule != null && logoutSpotModule.isGhost((Entity)(Object)remotePlayer)) {
                    callbackInfo.cancel();
                }
            }
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }

    @Inject(method={"turn"}, at={@At(value="HEAD")}, cancellable=true)
    public void onTurn(double d, double d2, CallbackInfo callbackInfo) {
        try {
            if ((Object)this == EntityMixin.mc.player && Night.MODULE_MANAGER != null) {
                IFreecamModule iFreecamModule = (IFreecamModule)Night.MODULE_MANAGER.getModule("Freecam");
                if (iFreecamModule != null && iFreecamModule.isToggled() && !iFreecamModule.isRotate()) {
                    iFreecamModule.onMouseTurn(d, d2);
                    callbackInfo.cancel();
                    return;
                }
                ElytraFlyModule elytraFlyModule = (ElytraFlyModule)Night.MODULE_MANAGER.getModule(ElytraFlyModule.class);
                if (elytraFlyModule != null && elytraFlyModule.isFreeLookActive()) {
                    elytraFlyModule.onMouseTurn(d, d2);
                    callbackInfo.cancel();
                    return;
                }
                AutoMaceModule autoMaceModule = (AutoMaceModule)Night.MODULE_MANAGER.getModule(AutoMaceModule.class);
                if (autoMaceModule != null && autoMaceModule.isFreeLookActive()) {
                    autoMaceModule.onMouseTurn(d, d2);
                    callbackInfo.cancel();
                }
            }
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }

    @Inject(method={"push(Lnet/minecraft/world/entity/Entity;)V"}, at={@At(value="HEAD")}, cancellable=true)
    private void pushAwayFrom(Entity entity, CallbackInfo callbackInfo) {
        try {
            if (!((Object)this == EntityMixin.mc.player && this.isPhantomEntity(entity) || entity == EntityMixin.mc.player && this.isPhantomEntity((Entity)(Object)this))) {
                if ((Object)this == EntityMixin.mc.player && ((VelocityModule)Night.MODULE_MANAGER.getModule(VelocityModule.class)).isToggled() && ((VelocityModule)Night.MODULE_MANAGER.getModule(VelocityModule.class)).antiPush.getValue()) {
                    callbackInfo.cancel();
                }
            } else {
                callbackInfo.cancel();
            }
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }

    private boolean isPhantomEntity(Entity entity) {
        if (EntityUtils.isGhost((Entity)entity)) {
            return true;
        }
        FakePlayerModule fakePlayerModule = (FakePlayerModule)Night.MODULE_MANAGER.getModule(FakePlayerModule.class);
        return fakePlayerModule != null && entity == fakePlayerModule.getPlayer();
    }

    @WrapOperation(method={"updateFluidInteraction"}, at={@At(value="INVOKE", target="Lnet/minecraft/world/entity/EntityFluidInteraction;applyCurrentTo(Lnet/minecraft/tags/TagKey;Lnet/minecraft/world/entity/Entity;D)V")})
    private void updateMovementInFluid(EntityFluidInteraction entityFluidInteraction, TagKey<Fluid> tagKey, Entity entity, double d, Operation<Void> operation) {
        if ((Object)this == EntityMixin.mc.player && ((VelocityModule)Night.MODULE_MANAGER.getModule(VelocityModule.class)).isToggled() && ((VelocityModule)Night.MODULE_MANAGER.getModule(VelocityModule.class)).antiLiquidPush.getValue()) {
            return;
        }
        operation.call(new Object[]{entityFluidInteraction, tagKey, entity, d});
    }

    @Inject(method={"moveRelative"}, at={@At(value="HEAD")}, cancellable=true)
    private void updateVelocity(float f, Vec3 vec3, CallbackInfo callbackInfo) {
        try {
            if ((Object)this != EntityMixin.mc.player) {
                return;
            }
            UpdateVelocityEvent updateVelocityEvent = new UpdateVelocityEvent(vec3, f);
            Night.EVENT_HANDLER.post((Event)updateVelocityEvent);
            if (updateVelocityEvent.isCancelled()) {
                callbackInfo.cancel();
                EntityMixin.mc.player.setDeltaMovement(EntityMixin.mc.player.getDeltaMovement().add(updateVelocityEvent.getVelocity()));
            }
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }

    @Inject(method={"getYRot()F"}, at={@At(value="HEAD")}, cancellable=true)
    private void getYaw(CallbackInfoReturnable<Float> callbackInfoReturnable) {
        try {
            if (RotationManager.swapActive && (Object)this == EntityMixin.mc.player) {
                return;
            }
            RotationLockModule rotationLockModule = (RotationLockModule)Night.MODULE_MANAGER.getModule(RotationLockModule.class);
            if (rotationLockModule.isToggled() && (rotationLockModule.mode.getValue().equals("Yaw") || rotationLockModule.mode.getValue().equals("Both")) && (Object)this == EntityMixin.mc.player) {
                callbackInfoReturnable.setReturnValue(rotationLockModule.getYawValue(this.yRot));
            }
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }

    @Inject(method={"getXRot()F"}, at={@At(value="HEAD")}, cancellable=true)
    private void getPitchRaw(CallbackInfoReturnable<Float> callbackInfoReturnable) {
        try {
            if (RotationManager.swapActive && (Object)this == EntityMixin.mc.player) {
                return;
            }
            RotationLockModule rotationLockModule = (RotationLockModule)Night.MODULE_MANAGER.getModule(RotationLockModule.class);
            if (rotationLockModule.isToggled() && (rotationLockModule.mode.getValue().equals("Pitch") || rotationLockModule.mode.getValue().equals("Both")) && (Object)this == EntityMixin.mc.player) {
                callbackInfoReturnable.setReturnValue(rotationLockModule.getPitchValue(this.xRot));
            }
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }

    @Inject(method={"setYRot"}, at={@At(value="HEAD")}, cancellable=true)
    private void setYaw(float f, CallbackInfo callbackInfo) {
        try {
            if ((Object)this != EntityMixin.mc.player) {
                return;
            }
            Night.EVENT_HANDLER.post((Event)new ChangeYawEvent(f));
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }

    @Inject(method={"setXRot"}, at={@At(value="HEAD")}, cancellable=true)
    private void setPitch(float f, CallbackInfo callbackInfo) {
        try {
            if ((Object)this != EntityMixin.mc.player) {
                return;
            }
            Night.EVENT_HANDLER.post((Event)new ChangePitchEvent(f));
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }

    @ModifyExpressionValue(method={"getBlockSpeedFactor"}, at={@At(value="INVOKE", target="Lnet/minecraft/world/level/block/state/BlockState;getBlock()Lnet/minecraft/world/level/block/Block;")})
    private Block getVelocityMultiplier(Block block) {
        try {
            if (((NoSlowModule)Night.MODULE_MANAGER.getModule(NoSlowModule.class)).isToggled() && (block == Blocks.SOUL_SAND && ((NoSlowModule)Night.MODULE_MANAGER.getModule(NoSlowModule.class)).soulSand.getValue() || block == Blocks.HONEY_BLOCK && ((NoSlowModule)Night.MODULE_MANAGER.getModule(NoSlowModule.class)).honeyBlocks.getValue())) {
                return Blocks.STONE;
            }
            return block;
        }
        catch (NullPointerException nullPointerException) {
            return block;
        }
    }

    @ModifyReturnValue(method={"isSprinting()Z"}, at={@At(value="RETURN")})
    private boolean injectIsSprinting(boolean bl) {
        try {
            if ((Object)this != EntityMixin.mc.player || Night.MODULE_MANAGER == null) {
                return bl;
            }
            ElytraFlyModule elytraFlyModule = (ElytraFlyModule)Night.MODULE_MANAGER.getModule(ElytraFlyModule.class);
            if (elytraFlyModule != null && elytraFlyModule.isBounceActive() && elytraFlyModule.isGliding()) {
                return true;
            }
            return bl;
        }
        catch (NullPointerException nullPointerException) {
            return bl;
        }
    }

    @ModifyReturnValue(method={"getPose"}, at={@At(value="RETURN")})
    private Pose injectGetPose(Pose pose) {
        try {
            if ((Object)this != EntityMixin.mc.player || Night.MODULE_MANAGER == null) {
                return pose;
            }
            ElytraFlyModule elytraFlyModule = (ElytraFlyModule)Night.MODULE_MANAGER.getModule(ElytraFlyModule.class);
            if (elytraFlyModule != null && elytraFlyModule.isBounceActive() && elytraFlyModule.isGliding()) {
                return Pose.FALL_FLYING;
            }
            return pose;
        }
        catch (NullPointerException nullPointerException) {
            return pose;
        }
    }
}

