/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lightning.Night
 *  lightning.events.Event
 *  lightning.events.impl.AttackBlockEvent
 *  lightning.events.impl.AttackEntityEvent
 *  lightning.events.impl.BreakBlockEvent
 *  lightning.modules.impl.core.NoMiddleClickModule
 *  lightning.modules.impl.movement.InventoryControlModule
 *  lightning.modules.impl.player.NoBreakDelayModule
 *  lightning.modules.impl.player.NoInteractModule
 *  lightning.pingbypass.PingBypassFlags
 *  lightning.utils.minecraft.WorldUtils
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.multiplayer.MultiPlayerGameMode
 *  net.minecraft.client.player.LocalPlayer
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket
 *  net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket$Action
 *  net.minecraft.network.protocol.game.ServerboundPlayerInputPacket
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.player.Input
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.inventory.ContainerInput
 *  net.minecraft.world.phys.BlockHitResult
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.At$Shift
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package lightning.mixins;

import lightning.Night;
import lightning.events.Event;
import lightning.events.impl.AttackBlockEvent;
import lightning.events.impl.AttackEntityEvent;
import lightning.events.impl.BreakBlockEvent;
import lightning.mixins.accessors.ClientPlayerEntityAccessor;
import lightning.modules.impl.core.NoMiddleClickModule;
import lightning.modules.impl.movement.InventoryControlModule;
import lightning.modules.impl.player.NoBreakDelayModule;
import lightning.modules.impl.player.NoInteractModule;
import lightning.pingbypass.PingBypassFlags;
import lightning.utils.minecraft.WorldUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={MultiPlayerGameMode.class})
public class ClientPlayerInteractionManagerMixin {
    @Shadow
    @Final
    private Minecraft minecraft;
    @Shadow
    private int destroyDelay;
    @Unique
    private boolean night$stoppedSprintForClick = false;

    @Inject(method={"handleContainerInput"}, at={@At(value="HEAD")})
    private void beforeContainerClick(int containerId, int slotNum, int buttonNum, ContainerInput containerInput, Player player, CallbackInfo callbackInfo) {
        try {
            this.night$stoppedSprintForClick = false;
            InventoryControlModule inventoryControlModule = (InventoryControlModule)Night.MODULE_MANAGER.getModule(InventoryControlModule.class);
            if (!inventoryControlModule.isToggled() || !inventoryControlModule.grimV2.getValue()) {
                return;
            }
            if (this.minecraft.player == null || this.minecraft.getConnection() == null) {
                return;
            }
            Input input = this.minecraft.player.input.keyPresses;
            if (input.forward() || input.backward() || input.left() || input.right() || input.jump()) {
                Input input2 = new Input(false, false, false, false, false, input.shift(), input.sprint());
                this.minecraft.getConnection().send((Packet)new ServerboundPlayerInputPacket(input2));
                ((ClientPlayerEntityAccessor)this.minecraft.player).setLastSentInput(input2);
            }
            if (this.minecraft.player.isSprinting()) {
                this.minecraft.getConnection().send((Packet)new ServerboundPlayerCommandPacket((Entity)this.minecraft.player, ServerboundPlayerCommandPacket.Action.STOP_SPRINTING));
                this.night$stoppedSprintForClick = true;
            }
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }

    @Inject(method={"handleContainerInput"}, at={@At(value="TAIL")})
    private void afterContainerClick(int containerId, int slotNum, int buttonNum, ContainerInput containerInput, Player player, CallbackInfo callbackInfo) {
        if (!this.night$stoppedSprintForClick) {
            return;
        }
        this.night$stoppedSprintForClick = false;
        if (this.minecraft.player == null || this.minecraft.getConnection() == null) {
            return;
        }
        this.minecraft.getConnection().send((Packet)new ServerboundPlayerCommandPacket((Entity)this.minecraft.player, ServerboundPlayerCommandPacket.Action.START_SPRINTING));
    }

    @Inject(method={"ensureHasSentCarriedItem"}, at={@At(value="HEAD")}, cancellable=true)
    private void syncSelectedSlot(CallbackInfo callbackInfo) {
        try {
            if (PingBypassFlags.proxyForwardingActive && Night.PINGBYPASS_CONFIG != null && Night.PINGBYPASS_CONFIG.isServer()) {
                callbackInfo.cancel();
            }
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }

    @Inject(method={"releaseUsingItem"}, at={@At(value="HEAD")}, cancellable=true)
    private void stopUsingItem(CallbackInfo callbackInfo) {
        try {
            if (PingBypassFlags.proxyForwardingActive && Night.PINGBYPASS_CONFIG != null && Night.PINGBYPASS_CONFIG.isServer()) {
                callbackInfo.cancel();
            }
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }

    @Inject(method={"tick"}, at={@At(value="HEAD")})
    private void onTick(CallbackInfo callbackInfo) {
        try {
            NoBreakDelayModule noBreakDelayModule;
            if (Night.MODULE_MANAGER != null && (noBreakDelayModule = (NoBreakDelayModule)Night.MODULE_MANAGER.getModule(NoBreakDelayModule.class)) != null && noBreakDelayModule.isToggled() && this.destroyDelay > noBreakDelayModule.delay.getValue().intValue()) {
                this.destroyDelay = noBreakDelayModule.delay.getValue().intValue();
            }
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }

    @Inject(method={"continueDestroyBlock"}, at={@At(value="HEAD")})
    private void onContinueDestroyBlock(BlockPos blockPos, Direction direction, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        try {
            NoBreakDelayModule noBreakDelayModule;
            if (Night.MODULE_MANAGER != null && (noBreakDelayModule = (NoBreakDelayModule)Night.MODULE_MANAGER.getModule(NoBreakDelayModule.class)) != null && noBreakDelayModule.isToggled() && this.destroyDelay > noBreakDelayModule.delay.getValue().intValue()) {
                this.destroyDelay = noBreakDelayModule.delay.getValue().intValue();
            }
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }

    @Inject(method={"startDestroyBlock"}, at={@At(value="HEAD")}, cancellable=true)
    private void attackBlock(BlockPos blockPos, Direction direction, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        try {
            NoBreakDelayModule noBreakDelayModule;
            if (Night.MODULE_MANAGER != null && (noBreakDelayModule = (NoBreakDelayModule)Night.MODULE_MANAGER.getModule(NoBreakDelayModule.class)) != null && noBreakDelayModule.isToggled() && this.destroyDelay > noBreakDelayModule.delay.getValue().intValue()) {
                this.destroyDelay = noBreakDelayModule.delay.getValue().intValue();
            }
            AttackBlockEvent attackBlockEvent = new AttackBlockEvent(blockPos, direction);
            Night.EVENT_HANDLER.post((Event)attackBlockEvent);
            if (attackBlockEvent.isCancelled()) {
                callbackInfoReturnable.setReturnValue(false);
            }
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }

    @Inject(method={"useItemOn"}, at={@At(value="HEAD")}, cancellable=true)
    private void interactBlock(LocalPlayer localPlayer, InteractionHand interactionHand, BlockHitResult blockHitResult, CallbackInfoReturnable<InteractionResult> callbackInfoReturnable) {
        try {
            NoInteractModule noInteractModule = (NoInteractModule)Night.MODULE_MANAGER.getModule(NoInteractModule.class);
            if (noInteractModule.isToggled() && noInteractModule.shouldNoInteract() && noInteractModule.mode.getValue().equalsIgnoreCase("Disable") && WorldUtils.RIGHT_CLICKABLE_BLOCKS.contains(this.minecraft.level.getBlockState(blockHitResult.getBlockPos()).getBlock())) {
                callbackInfoReturnable.setReturnValue(InteractionResult.FAIL);
            }
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }

    @Inject(method={"useItemOn"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;startPrediction(Lnet/minecraft/client/multiplayer/ClientLevel;Lnet/minecraft/client/multiplayer/prediction/PredictiveAction;)V")})
    private void interactBlock$BEFORE(LocalPlayer localPlayer, InteractionHand interactionHand, BlockHitResult blockHitResult, CallbackInfoReturnable<InteractionResult> callbackInfoReturnable) {
        try {
            NoInteractModule noInteractModule = (NoInteractModule)Night.MODULE_MANAGER.getModule(NoInteractModule.class);
            if (!this.minecraft.player.isShiftKeyDown() && noInteractModule.isToggled() && noInteractModule.shouldNoInteract() && noInteractModule.mode.getValue().equalsIgnoreCase("Sneak") && WorldUtils.RIGHT_CLICKABLE_BLOCKS.contains(this.minecraft.level.getBlockState(blockHitResult.getBlockPos()).getBlock())) {
                this.minecraft.player.connection.send((Packet)new ServerboundPlayerInputPacket(new Input(false, false, false, false, false, true, false)));
            }
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }

    @Inject(method={"useItemOn"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;startPrediction(Lnet/minecraft/client/multiplayer/ClientLevel;Lnet/minecraft/client/multiplayer/prediction/PredictiveAction;)V", shift=At.Shift.AFTER)})
    private void interactBlock$AFTER(LocalPlayer localPlayer, InteractionHand interactionHand, BlockHitResult blockHitResult, CallbackInfoReturnable<InteractionResult> callbackInfoReturnable) {
        try {
            NoInteractModule noInteractModule = (NoInteractModule)Night.MODULE_MANAGER.getModule(NoInteractModule.class);
            if (!this.minecraft.player.isShiftKeyDown() && noInteractModule.isToggled() && noInteractModule.shouldNoInteract() && noInteractModule.mode.getValue().equalsIgnoreCase("Sneak") && WorldUtils.RIGHT_CLICKABLE_BLOCKS.contains(this.minecraft.level.getBlockState(blockHitResult.getBlockPos()).getBlock())) {
                this.minecraft.player.connection.send((Packet)new ServerboundPlayerInputPacket(new Input(false, false, false, false, false, false, false)));
            }
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }

    @Inject(method={"attack"}, at={@At(value="HEAD")})
    private void attackEntity(Player player, Entity entity, CallbackInfo callbackInfo) {
        try {
            Night.EVENT_HANDLER.post((Event)new AttackEntityEvent(player, entity));
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }

    @Inject(method={"destroyBlock"}, at={@At(value="HEAD")})
    private void breakBlock(BlockPos blockPos, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        try {
            Night.EVENT_HANDLER.post((Event)new BreakBlockEvent(blockPos));
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }

    @Inject(method={"destroyBlock"}, at={@At(value="TAIL")})
    private void afterBreakBlock(BlockPos blockPos, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        try {
            NoBreakDelayModule noBreakDelayModule;
            if (Night.MODULE_MANAGER != null && (noBreakDelayModule = (NoBreakDelayModule)Night.MODULE_MANAGER.getModule(NoBreakDelayModule.class)) != null && noBreakDelayModule.isToggled() && this.destroyDelay > noBreakDelayModule.delay.getValue().intValue()) {
                this.destroyDelay = noBreakDelayModule.delay.getValue().intValue();
            }
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }

    @Inject(method={"handlePickItemFromBlock"}, at={@At(value="HEAD")}, cancellable=true)
    private void onPickBlock(BlockPos blockPos, boolean includeData, CallbackInfo callbackInfo) {
        try {
            NoMiddleClickModule noMiddleClickModule;
            if (Night.MODULE_MANAGER != null && (noMiddleClickModule = (NoMiddleClickModule)Night.MODULE_MANAGER.getModule(NoMiddleClickModule.class)) != null && noMiddleClickModule.isToggled()) {
                callbackInfo.cancel();
            }
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }

    @Inject(method={"handlePickItemFromEntity"}, at={@At(value="HEAD")}, cancellable=true)
    private void onPickEntity(Entity entity, boolean includeData, CallbackInfo callbackInfo) {
        try {
            NoMiddleClickModule noMiddleClickModule;
            if (Night.MODULE_MANAGER != null && (noMiddleClickModule = (NoMiddleClickModule)Night.MODULE_MANAGER.getModule(NoMiddleClickModule.class)) != null && noMiddleClickModule.isToggled()) {
                callbackInfo.cancel();
            }
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }
}

