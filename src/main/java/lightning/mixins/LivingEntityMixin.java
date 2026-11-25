/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.ModifyExpressionValue
 *  com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod
 *  com.llamalad7.mixinextras.injector.wrapoperation.Operation
 *  com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation
 *  lightning.Night
 *  lightning.events.Event
 *  lightning.events.impl.ConsumeItemEvent
 *  lightning.events.impl.PlayerJumpEvent
 *  lightning.events.impl.PlayerJumpEvent$Post
 *  lightning.managers.RotationManager
 *  lightning.modules.api.IFastClimbModule
 *  lightning.modules.api.IHoleSnapModule
 *  lightning.modules.api.INoJumpDelayModule
 *  lightning.modules.api.ISprintModule
 *  lightning.modules.impl.combat.AutoMaceModule
 *  lightning.modules.impl.movement.ElytraFlyModule
 *  lightning.modules.impl.movement.StepModule
 *  lightning.modules.impl.player.SwingModule
 *  lightning.utils.IMinecraft
 *  net.minecraft.core.Holder
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.EquipmentSlot
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.ai.attributes.Attribute
 *  net.minecraft.world.entity.ai.attributes.AttributeInstance
 *  net.minecraft.world.entity.ai.attributes.AttributeModifier
 *  net.minecraft.world.entity.ai.attributes.Attributes
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.component.SwingAnimation
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.Vec3
 *  org.jetbrains.annotations.Nullable
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.At$Shift
 *  org.spongepowered.asm.mixin.injection.Constant
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.ModifyConstant
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package lightning.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import lightning.Night;
import lightning.events.Event;
import lightning.events.impl.ConsumeItemEvent;
import lightning.events.impl.PlayerJumpEvent;
import lightning.managers.RotationManager;
import lightning.mixins.accessors.EntityAccessor;
import lightning.modules.api.IFastClimbModule;
import lightning.modules.api.IHoleSnapModule;
import lightning.modules.api.INoJumpDelayModule;
import lightning.modules.api.ISprintModule;
import lightning.modules.impl.combat.AutoMaceModule;
import lightning.modules.impl.movement.ElytraFlyModule;
import lightning.modules.impl.movement.StepModule;
import lightning.modules.impl.player.SwingModule;
import lightning.utils.IMinecraft;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.SwingAnimation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={LivingEntity.class})
public abstract class LivingEntityMixin
extends Entity
implements IMinecraft {
    @Shadow
    @Final
    private static AttributeModifier SPEED_MODIFIER_SPRINTING;
    @Shadow
    private int noJumpDelay;
    @Shadow
    protected ItemStack useItem;
    @Unique
    private float night$grimYaw0;
    @Unique
    private boolean night$grimSwapped;

    @Shadow
    @Nullable
    public abstract AttributeInstance getAttribute(Holder<Attribute> attribute);

    public LivingEntityMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @WrapMethod(method={"maxUpStep"})
    private float getStepHeight(Operation<Float> operation) {
        IHoleSnapModule iHoleSnapModule = Night.MODULE_MANAGER != null ? (IHoleSnapModule)Night.MODULE_MANAGER.getModule("HoleSnap") : null;
        IHoleSnapModule unusedHoleSnapModule = iHoleSnapModule;
        if ((Object)this == LivingEntityMixin.mc.player && Night.MODULE_MANAGER != null && (((StepModule)Night.MODULE_MANAGER.getModule(StepModule.class)).isToggled() && LivingEntityMixin.mc.player.onGround() || iHoleSnapModule != null && iHoleSnapModule.isToggled() && iHoleSnapModule.isStepEnabled())) {
            return ((StepModule)Night.MODULE_MANAGER.getModule(StepModule.class)).height.getValue().floatValue();
        }
        return ((Float)operation.call(new Object[0])).floatValue();
    }

    @Inject(method={"aiStep"}, at={@At(value="INVOKE", target="Lnet/minecraft/util/profiling/ProfilerFiller;pop()V", ordinal=2, shift=At.Shift.BEFORE)})
    private void doItemUse(CallbackInfo callbackInfo) {
        try {
            INoJumpDelayModule iNoJumpDelayModule = Night.MODULE_MANAGER != null ? (INoJumpDelayModule)Night.MODULE_MANAGER.getModule("NoJumpDelay") : null;
            INoJumpDelayModule unusedNoJumpDelayModule = iNoJumpDelayModule;
            if (iNoJumpDelayModule != null && iNoJumpDelayModule.isToggled() && this.noJumpDelay == 10) {
                this.noJumpDelay = iNoJumpDelayModule.getTicksValue();
            }
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }

    @Inject(method={"aiStep"}, at={@At(value="RETURN")})
    private void night$bounceNoJumpDelay(CallbackInfo callbackInfo) {
        try {
            if ((Object)this != LivingEntityMixin.mc.player || Night.MODULE_MANAGER == null) {
                return;
            }
            ElytraFlyModule elytraFlyModule = (ElytraFlyModule)Night.MODULE_MANAGER.getModule(ElytraFlyModule.class);
            if (elytraFlyModule != null && elytraFlyModule.isBounceActive()) {
                this.noJumpDelay = 0;
            }
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }

    @WrapOperation(method={"getCurrentSwingDuration"}, at={@At(value="INVOKE", target="Lnet/minecraft/world/item/component/SwingAnimation;duration()I")})
    private int getHandSwingDuration(SwingAnimation swingAnimation, Operation<Integer> operation) {
        int duration = (Integer)operation.call(new Object[]{swingAnimation});
        if ((Object)this != LivingEntityMixin.mc.player) {
            return duration;
        }
        return ((SwingModule)Night.MODULE_MANAGER.getModule(SwingModule.class)).isToggled() && ((SwingModule)Night.MODULE_MANAGER.getModule(SwingModule.class)).modifySpeed.getValue() && LivingEntityMixin.mc.options.getCameraType().isFirstPerson() ? 21 - ((SwingModule)Night.MODULE_MANAGER.getModule(SwingModule.class)).speed.getValue().intValue() : duration;
    }

    @Inject(method={"completeUsingItem"}, at={@At(value="INVOKE", target="Lnet/minecraft/world/item/ItemStack;finishUsingItem(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;)Lnet/minecraft/world/item/ItemStack;", shift=At.Shift.AFTER)})
    private void consumeItem(CallbackInfo callbackInfo) {
        try {
            if ((Object)this == LivingEntityMixin.mc.player) {
                Night.EVENT_HANDLER.post((Event)new ConsumeItemEvent(this.useItem));
            }
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }

    @Inject(method={"setSprinting"}, at={@At(value="INVOKE", target="Lnet/minecraft/world/entity/Entity;setSprinting(Z)V", shift=At.Shift.AFTER)}, cancellable=true)
    private void setSprinting$setSprinting(boolean sprinting, CallbackInfo callbackInfo) {
        try {
            if ((Object)this == LivingEntityMixin.mc.player) {
                ElytraFlyModule elytraFlyModule = Night.MODULE_MANAGER != null ? (ElytraFlyModule)Night.MODULE_MANAGER.getModule(ElytraFlyModule.class) : null;
                ElytraFlyModule unusedElytraFlyModule = elytraFlyModule;
                if (elytraFlyModule != null && elytraFlyModule.isToggled() && elytraFlyModule.mode.getValue().equalsIgnoreCase("Bounce")) {
                    return;
                }
                ISprintModule iSprintModule = Night.MODULE_MANAGER == null ? null : (ISprintModule)Night.MODULE_MANAGER.getModule("Sprint");
                ISprintModule unusedSprintModule = iSprintModule;
                if (iSprintModule != null && iSprintModule.isToggled()) {
                    AttributeInstance attributeInstance = this.getAttribute((Holder<Attribute>)Attributes.MOVEMENT_SPEED);
                    if (attributeInstance != null) {
                        attributeInstance.removeModifier(SPEED_MODIFIER_SPRINTING.id());
                        if (iSprintModule.shouldSprint()) {
                            this.setSharedFlag(3, true);
                            attributeInstance.addTransientModifier(SPEED_MODIFIER_SPRINTING);
                        } else {
                            this.setSharedFlag(3, false);
                        }
                    }
                    callbackInfo.cancel();
                }
            }
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }

    @Inject(method={"jumpFromGround"}, at={@At(value="HEAD")})
    private void jump$HEAD(CallbackInfo callbackInfo) {
        try {
            if ((Object)this != LivingEntityMixin.mc.player) {
                return;
            }
            Night.EVENT_HANDLER.post((Event)new PlayerJumpEvent());
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }

    @Inject(method={"jumpFromGround"}, at={@At(value="RETURN")})
    private void jump$RETURN(CallbackInfo callbackInfo) {
        try {
            if ((Object)this != LivingEntityMixin.mc.player) {
                return;
            }
            Night.EVENT_HANDLER.post((Event)new PlayerJumpEvent.Post());
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }

    @Inject(method={"onClimbable"}, at={@At(value="HEAD")}, cancellable=true)
    private void night$fastClimbIgnore(CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        try {
            if ((Object)this != LivingEntityMixin.mc.player) {
                return;
            }
            IFastClimbModule iFastClimbModule = (IFastClimbModule)Night.MODULE_MANAGER.getModule("FastClimb");
            if (iFastClimbModule.isToggled() && iFastClimbModule.getSpeedValue() == 0.0f) {
                callbackInfoReturnable.setReturnValue(false);
            }
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }

    @ModifyConstant(method={"handleOnClimbable"}, constant={@Constant(floatValue=0.15f)}, require=0)
    private float night$fastClimbScale(float climbSpeed) {
        return this.night$fastClimbFactor(climbSpeed);
    }

    @Unique
    private float night$fastClimbFactor(float baseClimbSpeed) {
        if ((Object)this != LivingEntityMixin.mc.player) {
            return baseClimbSpeed;
        }
        IFastClimbModule iFastClimbModule = (IFastClimbModule)Night.MODULE_MANAGER.getModule("FastClimb");
        if (!iFastClimbModule.isToggled()) {
            return baseClimbSpeed;
        }
        float speedMultiplier = iFastClimbModule.getSpeedValue();
        if (speedMultiplier <= 0.0f || speedMultiplier >= 1.0f) {
            return baseClimbSpeed;
        }
        return baseClimbSpeed * speedMultiplier;
    }

    @Inject(method={"getItemBySlot"}, at={@At(value="HEAD")}, cancellable=true)
    private void night$hideElytraSwap(EquipmentSlot equipmentSlot, CallbackInfoReturnable<ItemStack> callbackInfoReturnable) {
        try {
            if (equipmentSlot != EquipmentSlot.CHEST || (Object)this != LivingEntityMixin.mc.player || Night.MODULE_MANAGER == null) {
                return;
            }
            ItemStack itemStack = ((ElytraFlyModule)Night.MODULE_MANAGER.getModule(ElytraFlyModule.class)).getGrimHiddenElytra();
            if (itemStack != null) {
                callbackInfoReturnable.setReturnValue(itemStack);
            }
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }

    @Inject(method={"isFallFlying"}, at={@At(value="HEAD")}, cancellable=true)
    private void night$pinBounceAndControlRocketGlide(CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        try {
            AutoMaceModule autoMaceModule;
            if ((Object)this != LivingEntityMixin.mc.player || Night.MODULE_MANAGER == null) {
                return;
            }
            ElytraFlyModule elytraFlyModule = (ElytraFlyModule)Night.MODULE_MANAGER.getModule(ElytraFlyModule.class);
            if (elytraFlyModule != null && elytraFlyModule.isToggled()) {
                if (elytraFlyModule.isBounceActive() && elytraFlyModule.isGliding()) {
                    callbackInfoReturnable.setReturnValue(true);
                    return;
                }
                if (elytraFlyModule.shouldPinFallFlying()) {
                    callbackInfoReturnable.setReturnValue(true);
                    return;
                }
            }
            if ((autoMaceModule = (AutoMaceModule)Night.MODULE_MANAGER.getModule(AutoMaceModule.class)) != null && autoMaceModule.shouldPinFallFlying()) {
                callbackInfoReturnable.setReturnValue(true);
            }
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }

    @WrapOperation(method={"updateFallFlyingMovement"}, at={@At(value="INVOKE", target="Lnet/minecraft/world/entity/LivingEntity;getXRot()F")})
    private float wrapFallFlyingPitch(LivingEntity livingEntity, Operation<Float> operation) {
        ElytraFlyModule elytraFlyModule;
        if ((Object)this == LivingEntityMixin.mc.player && Night.MODULE_MANAGER != null && (elytraFlyModule = (ElytraFlyModule)Night.MODULE_MANAGER.getModule(ElytraFlyModule.class)) != null && elytraFlyModule.isBounceAutoPitch()) {
            return elytraFlyModule.getBouncePitch();
        }
        return ((Float)operation.call(new Object[]{livingEntity})).floatValue();
    }

    @WrapOperation(method={"updateFallFlyingMovement"}, at={@At(value="INVOKE", target="Lnet/minecraft/world/entity/LivingEntity;getLookAngle()Lnet/minecraft/world/phys/Vec3;")})
    private Vec3 wrapFallFlyingLookAngle(LivingEntity livingEntity, Operation<Vec3> operation) {
        ElytraFlyModule elytraFlyModule;
        if ((Object)this == LivingEntityMixin.mc.player && Night.MODULE_MANAGER != null && (elytraFlyModule = (ElytraFlyModule)Night.MODULE_MANAGER.getModule(ElytraFlyModule.class)) != null && elytraFlyModule.isBounceAutoPitch()) {
            return Vec3.directionFromRotation((float)elytraFlyModule.getBouncePitch(), (float)livingEntity.getYRot());
        }
        return (Vec3)operation.call(new Object[]{livingEntity});
    }

    @Inject(method={"travel"}, at={@At(value="HEAD")})
    private void travel$grimHead(Vec3 vec3, CallbackInfo callbackInfo) {
        try {
            this.night$grimSwapped = false;
            if ((Object)this == LivingEntityMixin.mc.player && this.night$shouldSpoofRotation()) {
                this.night$grimYaw0 = ((EntityAccessor)((Object)this)).getRawYRot();
                ((EntityAccessor)((Object)this)).setRawYRot(this.night$spoofYaw());
                RotationManager.swapActive = true;
                this.night$grimSwapped = true;
            }
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }

    @Inject(method={"travel"}, at={@At(value="RETURN")})
    private void travel$grimReturn(Vec3 vec3, CallbackInfo callbackInfo) {
        try {
            if (!this.night$grimSwapped) {
                return;
            }
            this.night$grimSwapped = false;
            RotationManager.swapActive = false;
            ((EntityAccessor)((Object)this)).setRawYRot(this.night$grimYaw0);
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }

    @ModifyExpressionValue(method={"jumpFromGround"}, at={@At(value="INVOKE", target="Lnet/minecraft/world/entity/LivingEntity;getYRot()F")})
    private float jumpFromGround$grimYaw(float yaw) {
        return (Object)this == LivingEntityMixin.mc.player && this.night$shouldSpoofRotation() ? this.night$spoofYaw() : yaw;
    }

    @Unique
    private boolean night$grimCompensating() {
        if (Night.MODULE_MANAGER == null) {
            return false;
        }
        ISprintModule iSprintModule = (ISprintModule)Night.MODULE_MANAGER.getModule("Sprint");
        return iSprintModule != null && iSprintModule.isGrimCompensating();
    }

    @Unique
    private boolean night$shouldSpoofRotation() {
        return this.night$grimCompensating();
    }

    @Unique
    private float night$spoofYaw() {
        return ((ISprintModule)Night.MODULE_MANAGER.getModule("Sprint")).getGrimYaw();
    }
}

