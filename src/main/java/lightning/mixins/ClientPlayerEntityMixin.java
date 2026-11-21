/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.ModifyExpressionValue
 *  com.llamalad7.mixinextras.injector.wrapoperation.Operation
 *  com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation
 *  com.mojang.authlib.GameProfile
 *  lightning.Night
 *  lightning.events.Event
 *  lightning.events.impl.ChangeHandEvent
 *  lightning.events.impl.PlayerMoveEvent
 *  lightning.events.impl.PlayerUpdateEvent
 *  lightning.events.impl.SendMovementEvent
 *  lightning.events.impl.UpdateMovementEvent
 *  lightning.events.impl.UpdateMovementEvent$Post
 *  lightning.modules.api.ISprintModule
 *  lightning.modules.impl.movement.AirStuckModule
 *  lightning.modules.impl.movement.ElytraFlyModule
 *  lightning.modules.impl.movement.InventoryControlModule
 *  lightning.modules.impl.movement.NoSlowModule
 *  lightning.modules.impl.movement.VelocityModule
 *  lightning.modules.impl.player.NoEntityTraceModule
 *  lightning.modules.impl.player.SwingModule
 *  lightning.pingbypass.PingBypassFlags
 *  lightning.utils.BlockEntityInstanceTracker
 *  lightning.utils.EarlyTickHooks
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.multiplayer.ClientLevel
 *  net.minecraft.client.multiplayer.ClientPacketListener
 *  net.minecraft.client.player.AbstractClientPlayer
 *  net.minecraft.client.player.ClientInput
 *  net.minecraft.client.player.LocalPlayer
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ServerboundSwingPacket
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.entity.MoverType
 *  net.minecraft.world.phys.EntityHitResult
 *  net.minecraft.world.phys.Vec3
 *  org.jetbrains.annotations.Nullable
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.At$Shift
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package lightning.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.authlib.GameProfile;
import lightning.Night;
import lightning.events.Event;
import lightning.events.impl.ChangeHandEvent;
import lightning.events.impl.PlayerMoveEvent;
import lightning.events.impl.PlayerUpdateEvent;
import lightning.events.impl.SendMovementEvent;
import lightning.events.impl.UpdateMovementEvent;
import lightning.modules.api.ISprintModule;
import lightning.modules.impl.movement.AirStuckModule;
import lightning.modules.impl.movement.ElytraFlyModule;
import lightning.modules.impl.movement.InventoryControlModule;
import lightning.modules.impl.movement.NoSlowModule;
import lightning.modules.impl.movement.VelocityModule;
import lightning.modules.impl.player.NoEntityTraceModule;
import lightning.modules.impl.player.SwingModule;
import lightning.pingbypass.PingBypassFlags;
import lightning.utils.BlockEntityInstanceTracker;
import lightning.utils.EarlyTickHooks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={LocalPlayer.class}, priority=500)
public abstract class ClientPlayerEntityMixin
extends AbstractClientPlayer {
    @Shadow
    @Final
    public ClientPacketListener connection;
    @Shadow
    private float xRotLast;
    @Shadow
    public float xBob;
    @Shadow
    public ClientInput input;

    public ClientPlayerEntityMixin(ClientLevel clientLevel, GameProfile gameProfile) {
        super(clientLevel, gameProfile);
    }

    @Shadow
    protected abstract void updateAutoJump(float var1, float var2);

    @Inject(method={"<init>"}, at={@At(value="HEAD")})
    private static void night$clearTrackedBlockEntities(CallbackInfo callbackInfo) {
        try {
            BlockEntityInstanceTracker.localPlayerConstructed();
            AirStuckModule.localPlayerConstructed();
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }

    @Inject(method={"tick"}, at={@At(value="HEAD")})
    private void earlyTick$dispatch(CallbackInfo callbackInfo) {
        try {
            if (Minecraft.getInstance().player != (Object)this) {
                return;
            }
            EarlyTickHooks.dispatch();
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }

    @Inject(method={"tick"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/player/AbstractClientPlayer;tick()V", shift=At.Shift.BEFORE)})
    private void tick$BEFORE(CallbackInfo callbackInfo) {
        try {
            Night.EVENT_HANDLER.post((Event)new PlayerUpdateEvent());
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }

    @Inject(method={"tick"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/player/AbstractClientPlayer;tick()V", shift=At.Shift.AFTER)})
    private void tick$AFTER(CallbackInfo callbackInfo) {
        try {
            Night.EVENT_HANDLER.post((Event)new UpdateMovementEvent());
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }

    @Inject(method={"tick"}, at={@At(value="TAIL")})
    private void tick$TAIL(CallbackInfo callbackInfo) {
        try {
            Night.EVENT_HANDLER.post((Event)new UpdateMovementEvent.Post());
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }

    @ModifyExpressionValue(method={"sendPosition"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/player/LocalPlayer;getXRot()F")})
    private float modifySendPositionPitch(float f) {
        try {
            ElytraFlyModule elytraFlyModule;
            if (Night.MODULE_MANAGER != null && (elytraFlyModule = (ElytraFlyModule)Night.MODULE_MANAGER.getModule(ElytraFlyModule.class)) != null && elytraFlyModule.isBounceAutoPitch()) {
                return elytraFlyModule.getBouncePitch();
            }
            return f;
        }
        catch (NullPointerException nullPointerException) {
            return f;
        }
    }

    @WrapOperation(method={"sendPosition"}, at={@At(value="FIELD", target="Lnet/minecraft/client/player/LocalPlayer;yRotLast:F", opcode=180, ordinal=0)})
    private float sendPosition$yRotLast(LocalPlayer localPlayer, Operation<Float> operation) {
        if (Night.ROTATION_MANAGER.getRotation() != null) {
            return Night.ROTATION_MANAGER.getServerYaw();
        }
        return ((Float)operation.call(new Object[]{localPlayer})).floatValue();
    }

    @WrapOperation(method={"sendPosition"}, at={@At(value="FIELD", target="Lnet/minecraft/client/player/LocalPlayer;xRotLast:F", opcode=180, ordinal=0)})
    private float sendPosition$xRotLast(LocalPlayer localPlayer, Operation<Float> operation) {
        if (Night.ROTATION_MANAGER.getRotation() != null) {
            return Night.ROTATION_MANAGER.getServerPitch();
        }
        return ((Float)operation.call(new Object[]{localPlayer})).floatValue();
    }

    @ModifyExpressionValue(method={"modifyInput"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/player/LocalPlayer;isUsingItem()Z")})
    private boolean tickMovement$isUsingItem(boolean bl) {
        try {
            if (((NoSlowModule)Night.MODULE_MANAGER.getModule(NoSlowModule.class)).isToggled() && ((NoSlowModule)Night.MODULE_MANAGER.getModule(NoSlowModule.class)).items.getValue() && !((NoSlowModule)Night.MODULE_MANAGER.getModule(NoSlowModule.class)).shouldSlow()) {
                return false;
            }
            return bl;
        }
        catch (NullPointerException nullPointerException) {
            return bl;
        }
    }

    @ModifyExpressionValue(method={"modifyInput"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/player/LocalPlayer;isMovingSlowly()Z")})
    private boolean tickMovement$isMovingSlowly(boolean bl) {
        try {
            if (!bl) {
                return bl;
            }
            NoSlowModule noSlowModule = (NoSlowModule)Night.MODULE_MANAGER.getModule(NoSlowModule.class);
            if (noSlowModule == null || !noSlowModule.isToggled()) {
                return bl;
            }
            if (this.isVisuallyCrawling() && noSlowModule.crawl.getValue()) {
                return false;
            }
            if (this.isCrouching() && noSlowModule.sneak.getValue()) {
                return false;
            }
            return bl;
        }
        catch (NullPointerException nullPointerException) {
            return bl;
        }
    }

    @Inject(method={"move"}, at={@At(value="HEAD")}, cancellable=true)
    private void move(MoverType moverType, Vec3 vec3, CallbackInfo callbackInfo) {
        try {
            if (PingBypassFlags.proxyForwardingActive && Night.PINGBYPASS_CONFIG != null && Night.PINGBYPASS_CONFIG.isServer()) {
                callbackInfo.cancel();
                return;
            }
            PlayerMoveEvent playerMoveEvent = new PlayerMoveEvent(moverType, vec3);
            Night.EVENT_HANDLER.post((Event)playerMoveEvent);
            if (playerMoveEvent.isCancelled()) {
                callbackInfo.cancel();
                double d = this.getX();
                double d2 = this.getZ();
                super.move(moverType, playerMoveEvent.getMovement());
                this.updateAutoJump((float)(this.getX() - d), (float)(this.getZ() - d2));
            }
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }

    @Inject(method={"sendPosition"}, at={@At(value="HEAD")}, cancellable=true)
    private void sendMovementPackets(CallbackInfo callbackInfo) {
        try {
            SendMovementEvent sendMovementEvent = new SendMovementEvent();
            Night.EVENT_HANDLER.post((Event)sendMovementEvent);
            if (sendMovementEvent.isCancelled()) {
                callbackInfo.cancel();
            }
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }

    @ModifyExpressionValue(method={"pick"}, at={@At(value="INVOKE", target="Lnet/minecraft/world/entity/projectile/ProjectileUtil;getEntityHitResult(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/AABB;Ljava/util/function/Predicate;D)Lnet/minecraft/world/phys/EntityHitResult;")})
    @Nullable
    private static EntityHitResult pick$getEntityHitResult(@Nullable EntityHitResult entityHitResult) {
        try {
            NoEntityTraceModule noEntityTraceModule = (NoEntityTraceModule)Night.MODULE_MANAGER.getModule(NoEntityTraceModule.class);
            if (noEntityTraceModule.isToggled() && noEntityTraceModule.shouldIgnore(entityHitResult != null ? entityHitResult.getEntity() : null)) {
                return null;
            }
            return entityHitResult;
        }
        catch (NullPointerException nullPointerException) {
            return entityHitResult;
        }
    }

    @Inject(method={"itemUseSpeedMultiplier"}, at={@At(value="HEAD")}, cancellable=true)
    private void onItemUseSpeedMultiplier(CallbackInfoReturnable<Float> callbackInfoReturnable) {
        try {
            NoSlowModule noSlowModule = Night.MODULE_MANAGER != null ? (NoSlowModule)Night.MODULE_MANAGER.getModule(NoSlowModule.class) : null;
            NoSlowModule noSlowModule2 = noSlowModule;
            if (noSlowModule != null && noSlowModule.isToggled() && noSlowModule.items.getValue() && !noSlowModule.shouldSlow()) {
                callbackInfoReturnable.setReturnValue(Float.valueOf(1.0f));
            }
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }

    @Inject(method={"shouldStopRunSprinting"}, at={@At(value="HEAD")}, cancellable=true)
    private void shouldStopRunSprinting$HEAD(CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        try {
            NoSlowModule noSlowModule = Night.MODULE_MANAGER != null ? (NoSlowModule)Night.MODULE_MANAGER.getModule(NoSlowModule.class) : null;
            NoSlowModule noSlowModule2 = noSlowModule;
            if (noSlowModule != null && noSlowModule.isToggled() && noSlowModule.items.getValue() && !noSlowModule.shouldSlow() && this.isUsingItem() && !this.isFallFlying() && !this.isMobilityRestricted() && !this.isPassenger()) {
                boolean bl;
                if (this.horizontalCollision && !this.minorHorizontalCollision) {
                    callbackInfoReturnable.setReturnValue(true);
                    return;
                }
                boolean bl2 = (float)this.getFoodData().getFoodLevel() > 6.0f || this.getAbilities().mayfly;
                boolean bl3 = bl2;
                if (!bl2) {
                    callbackInfoReturnable.setReturnValue(true);
                    return;
                }
                ISprintModule iSprintModule = Night.MODULE_MANAGER == null ? null : (ISprintModule)Night.MODULE_MANAGER.getModule("Sprint");
                ISprintModule iSprintModule2 = iSprintModule;
                boolean bl4 = iSprintModule != null && iSprintModule.isToggled() ? iSprintModule.shouldSprint() : (this.input != null && this.input.hasForwardImpulse());
                if (!bl4) {
                    callbackInfoReturnable.setReturnValue(true);
                    return;
                }
                if (this.isInWater() && !this.isUnderWater()) {
                    callbackInfoReturnable.setReturnValue(true);
                    return;
                }
                callbackInfoReturnable.setReturnValue(false);
            }
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }

    @Inject(method={"shouldStopRunSprinting"}, at={@At(value="RETURN")}, cancellable=true)
    private void shouldStopRunSprinting$RETURN(CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        try {
            if (((Boolean)callbackInfoReturnable.getReturnValue()).booleanValue()) {
                NoSlowModule noSlowModule = Night.MODULE_MANAGER != null ? (NoSlowModule)Night.MODULE_MANAGER.getModule(NoSlowModule.class) : null;
                NoSlowModule noSlowModule2 = noSlowModule;
                if (!(noSlowModule == null || !noSlowModule.isToggled() || !noSlowModule.items.getValue() || noSlowModule.shouldSlow() || !this.isUsingItem() || this.isFallFlying() || this.isMobilityRestricted() || this.isPassenger() || this.horizontalCollision && !this.minorHorizontalCollision)) {
                    boolean bl;
                    boolean bl2 = (float)this.getFoodData().getFoodLevel() > 6.0f || this.getAbilities().mayfly;
                    ISprintModule iSprintModule = Night.MODULE_MANAGER == null ? null : (ISprintModule)Night.MODULE_MANAGER.getModule("Sprint");
                    ISprintModule iSprintModule2 = iSprintModule;
                    boolean bl3 = iSprintModule != null && iSprintModule.isToggled() ? iSprintModule.shouldSprint() : (this.input != null && this.input.hasForwardImpulse());
                    if (bl2 && bl3 && (!this.isInWater() || this.isUnderWater())) {
                        callbackInfoReturnable.setReturnValue(false);
                    }
                }
            }
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }

    @Inject(method={"canStartSprinting"}, at={@At(value="HEAD")}, cancellable=true)
    private void canStartSprinting$HEAD(CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        try {
            NoSlowModule noSlowModule = Night.MODULE_MANAGER != null ? (NoSlowModule)Night.MODULE_MANAGER.getModule(NoSlowModule.class) : null;
            NoSlowModule noSlowModule2 = noSlowModule;
            if (noSlowModule != null && noSlowModule.isToggled() && noSlowModule.items.getValue() && !noSlowModule.shouldSlow() && this.isUsingItem() && !this.isSprinting() && !this.isFallFlying() && !this.isMobilityRestricted() && !this.isPassenger()) {
                boolean bl = (float)this.getFoodData().getFoodLevel() > 6.0f || this.getAbilities().mayfly;
                boolean bl2 = bl;
                if (bl) {
                    ISprintModule iSprintModule = Night.MODULE_MANAGER == null ? null : (ISprintModule)Night.MODULE_MANAGER.getModule("Sprint");
                    ISprintModule iSprintModule2 = iSprintModule;
                    boolean bl4 = iSprintModule != null && iSprintModule.isToggled() ? iSprintModule.shouldSprint() : (this.input != null && this.input.hasForwardImpulse());
                    if (bl4 && (!this.isInWater() || this.isUnderWater())) {
                        callbackInfoReturnable.setReturnValue(true);
                    }
                }
            }
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }

    @ModifyExpressionValue(method={"canStartSprinting"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/player/LocalPlayer;isSlowDueToUsingItem()Z")})
    private boolean canStartSprinting$isUsingItem(boolean bl) {
        try {
            NoSlowModule noSlowModule = Night.MODULE_MANAGER != null ? (NoSlowModule)Night.MODULE_MANAGER.getModule(NoSlowModule.class) : null;
            NoSlowModule noSlowModule2 = noSlowModule;
            if (noSlowModule != null && noSlowModule.isToggled() && noSlowModule.items.getValue() && !noSlowModule.shouldSlow()) {
                return false;
            }
            return bl;
        }
        catch (NullPointerException nullPointerException) {
            return bl;
        }
    }

    @Inject(method={"canStartSprinting"}, at={@At(value="RETURN")}, cancellable=true)
    private void canStartSprinting$RETURN(CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        try {
            if (!((Boolean)callbackInfoReturnable.getReturnValue()).booleanValue()) {
                NoSlowModule noSlowModule = Night.MODULE_MANAGER != null ? (NoSlowModule)Night.MODULE_MANAGER.getModule(NoSlowModule.class) : null;
                NoSlowModule noSlowModule2 = noSlowModule;
                if (noSlowModule != null && noSlowModule.isToggled() && noSlowModule.items.getValue() && !noSlowModule.shouldSlow() && this.isUsingItem() && !this.isSprinting() && !this.isFallFlying() && !this.isMobilityRestricted() && !this.isPassenger()) {
                    boolean bl = (float)this.getFoodData().getFoodLevel() > 6.0f || this.getAbilities().mayfly;
                    boolean bl2 = bl;
                    if (bl) {
                        ISprintModule iSprintModule = Night.MODULE_MANAGER == null ? null : (ISprintModule)Night.MODULE_MANAGER.getModule("Sprint");
                        ISprintModule iSprintModule2 = iSprintModule;
                        boolean bl4 = iSprintModule != null && iSprintModule.isToggled() ? iSprintModule.shouldSprint() : (this.input != null && this.input.hasForwardImpulse());
                        if (bl4 && (!this.isInWater() || this.isUnderWater())) {
                            callbackInfoReturnable.setReturnValue(true);
                        }
                    }
                }
            }
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }

    @Inject(method={"moveTowardsClosestSpace"}, at={@At(value="HEAD")}, cancellable=true)
    private void pushOutOfBlocks(double d, double d2, CallbackInfo callbackInfo) {
        try {
            if (((VelocityModule)Night.MODULE_MANAGER.getModule(VelocityModule.class)).isToggled() && ((VelocityModule)Night.MODULE_MANAGER.getModule(VelocityModule.class)).antiBlockPush.getValue()) {
                callbackInfo.cancel();
            }
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }

    @Inject(method={"swing"}, at={@At(value="HEAD")}, cancellable=true)
    private void swingHand(InteractionHand interactionHand, CallbackInfo callbackInfo) {
        try {
            if (((SwingModule)Night.MODULE_MANAGER.getModule(SwingModule.class)).isToggled()) {
                if (!((SwingModule)Night.MODULE_MANAGER.getModule(SwingModule.class)).hand.getValue().equals("None")) {
                    switch (((SwingModule)Night.MODULE_MANAGER.getModule(SwingModule.class)).hand.getValue()) {
                        case "Default": {
                            super.swing(interactionHand);
                            break;
                        }
                        case "Mainhand": {
                            super.swing(InteractionHand.MAIN_HAND);
                            break;
                        }
                        case "Offhand": {
                            super.swing(InteractionHand.OFF_HAND);
                            break;
                        }
                        case "Both": {
                            super.swing(InteractionHand.MAIN_HAND);
                            super.swing(InteractionHand.OFF_HAND);
                        }
                    }
                    if (((SwingModule)Night.MODULE_MANAGER.getModule(SwingModule.class)).hand.getValue().equalsIgnoreCase("Packet") || !((SwingModule)Night.MODULE_MANAGER.getModule(SwingModule.class)).noPacket.getValue()) {
                        this.connection.send((Packet)new ServerboundSwingPacket(interactionHand));
                    }
                }
                callbackInfo.cancel();
            }
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }

    @Inject(method={"handlePortalTransitionEffect"}, at={@At(value="HEAD")}, cancellable=true)
    private void tickNausea(boolean bl, CallbackInfo callbackInfo) {
        try {
            if (((InventoryControlModule)Night.MODULE_MANAGER.getModule(InventoryControlModule.class)).isToggled() && ((InventoryControlModule)Night.MODULE_MANAGER.getModule(InventoryControlModule.class)).portals.getValue()) {
                callbackInfo.cancel();
            }
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }

    @Inject(method={"startUsingItem"}, at={@At(value="HEAD")})
    private void setCurrentHand(InteractionHand interactionHand, CallbackInfo callbackInfo) {
        try {
            Night.EVENT_HANDLER.post((Event)new ChangeHandEvent());
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }
}

